package com.example.heartrateapp;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

public class PulseAnalyzer implements androidx.camera.core.ImageAnalysis.Analyzer {

    public interface PulseListener {
        void onPulseDetected(int bpm);
        void onSignalUpdate(double signalValue, boolean isPeak, boolean isSignalGood);
    }

    private final PulseListener listener;

    // --- NOWE ZMIENNE DO WYKRYWANIA PIKÓW (Maszyna Stanów) ---
    private double localMax = 0;
    private double localMin = 999;
    private boolean lookingForMin = true;
    private double dynamicThreshold = 2.0;
    // ---------------------------------------------------------

    private static final long MIN_TIME_BETWEEN_BEATS_MS = 400;
    private long lastBeatTime = 0;

    private final java.util.ArrayList<Integer> bpmHistory = new java.util.ArrayList<>();
    private static final int HISTORY_SIZE = 9;

    private final java.util.ArrayList<Double> smoothingBuffer = new java.util.ArrayList<>();
    private static final int SMOOTHING_WINDOW = 5;

    private final java.util.ArrayList<Double> sqaBuffer = new java.util.ArrayList<>();
    private static final int SQA_BUFFER_SIZE = 90;
    private boolean currentSignalGood = false;

    private long startTime = 0;
    private static final long WARMUP_TIME_MS = 3000;

    public PulseAnalyzer(PulseListener listener) {
        this.listener = listener;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        long currentTime = System.currentTimeMillis();

        if (startTime == 0) {
            startTime = currentTime;
        }
        if (currentTime - startTime < WARMUP_TIME_MS) {
            image.close();
            return;
        }

        try {
            java.nio.ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            int width = image.getWidth();
            int height = image.getHeight();

            long totalBrightness = 0;
            int validPixelCount = 0;

            for (int y = 0; y < height; y += 5) {
                for (int x = 0; x < width; x += 5) {
                    int index = y * width + x;
                    if (index < data.length) {
                        int pixelValue = data[index] & 0xFF;
                        if (pixelValue > 50 && pixelValue < 245) {
                            totalBrightness += pixelValue;
                            validPixelCount++;
                        }
                    }
                }
            }

            if (validPixelCount == 0) {
                currentSignalGood = false;
                if (listener != null) {
                    listener.onSignalUpdate(0, false, currentSignalGood);
                }
                image.close();
                return;
            }

            double currentRawValue = (double) totalBrightness / validPixelCount;

            smoothingBuffer.add(currentRawValue);
            if (smoothingBuffer.size() > SMOOTHING_WINDOW) {
                smoothingBuffer.remove(0);
            }
            double sum = 0;
            for (double val : smoothingBuffer) { sum += val; }
            double currentFiltered = sum / smoothingBuffer.size();

            // --- OCENA JAKOŚCI SYGNAŁU (SQA) ---
            sqaBuffer.add(currentFiltered);
            if (sqaBuffer.size() > SQA_BUFFER_SIZE) {
                sqaBuffer.remove(0);
            }

            if (sqaBuffer.size() == SQA_BUFFER_SIZE) {
                double sqaMin = Double.MAX_VALUE;
                double sqaMax = Double.MIN_VALUE;
                double sqaSum = 0;

                for(double v : sqaBuffer) {
                    if (v < sqaMin) sqaMin = v;
                    if (v > sqaMax) sqaMax = v;
                    sqaSum += v;
                }

                double sqaMean = sqaSum / SQA_BUFFER_SIZE;
                double sqaRange = sqaMax - sqaMin;

                int zeroCrossings = 0;
                boolean wasAbove = sqaBuffer.get(0) > sqaMean;

                for (int i = 1; i < SQA_BUFFER_SIZE; i++) {
                    boolean isAbove = sqaBuffer.get(i) > sqaMean;
                    if (isAbove != wasAbove) {
                        zeroCrossings++;
                        wasAbove = isAbove;
                    }
                }

                boolean hasGoodShape = (sqaRange > 1.5 && sqaRange < 80.0 && zeroCrossings >= 4 && zeroCrossings <= 35);

                // 2. Sprawdzamy, czy w ciągu ostatnich 2500 milisekund (2.5s) był jakiś pik
                boolean hasRecentPeaks = (currentTime - lastBeatTime) < 2500;

                // 3. Sygnał jest zielony TYLKO wtedy, gdy oba warunki są spełnione!
                if (hasGoodShape && hasRecentPeaks) {
                    currentSignalGood = true;
                } else {
                    currentSignalGood = false;
                }
            }

            // --- NOWY ALGORYTM DETEKCJI PIKÓW ---
            boolean isPeakNow = false;

            // Na bieżąco śledzimy najwyższy i najniższy punkt
            if (currentFiltered > localMax) localMax = currentFiltered;
            if (currentFiltered < localMin) localMin = currentFiltered;

            if (lookingForMin) {
                // Jeśli wykres odbił się od dna (localMin) o ustalony próg, mamy uderzenie serca!
                if (currentFiltered > localMin + dynamicThreshold) {

                    long timeDifference = currentTime - lastBeatTime;

                    // Odrzucamy echa szybsze niż 400ms
                    if (timeDifference > MIN_TIME_BETWEEN_BEATS_MS) {
                        isPeakNow = true; // Zaznaczamy kropkę na wykresie

                        // Liczymy BPM tylko jeśli sygnał ma dobre SQA (jest zielony)
                        if (currentSignalGood) {
                            double instantBpm = 60000.0 / timeDifference;

                            if (instantBpm > 45 && instantBpm < 170) {
                                bpmHistory.add((int) instantBpm);
                                if (bpmHistory.size() > HISTORY_SIZE) {
                                    bpmHistory.remove(0);
                                }

                                java.util.ArrayList<Integer> sortedHistory = new java.util.ArrayList<>(bpmHistory);
                                java.util.Collections.sort(sortedHistory);
                                int medianBpm = sortedHistory.get(sortedHistory.size() / 2);

                                if (listener != null) {
                                    listener.onPulseDetected(medianBpm);
                                }
                            }
                        }
                        lastBeatTime = currentTime;
                    }

                    // Znaleźliśmy dno, teraz szukamy kolejnego szczytu fali
                    lookingForMin = false;
                    localMax = currentFiltered;
                }
            } else {
                // Jeśli wykres opadł ze szczytu (localMax) o ustalony próg...
                if (currentFiltered < localMax - dynamicThreshold) {

                    // Zmieniamy tryb na szukanie uderzenia (dna)
                    lookingForMin = true;
                    localMin = currentFiltered;

                    // Aktualizujemy czułość (Auto-Kontrast) na podstawie ostatniej fali.
                    // 40% wysokości fali to bezpieczny margines odporny na szum.
                    double waveAmplitude = localMax - localMin;
                    if (waveAmplitude > 0.5) {
                        dynamicThreshold = waveAmplitude * 0.4;
                    }
                }
            }

            // WYSYŁANIE DANYCH DO WYKRESU
            if (listener != null) {
                listener.onSignalUpdate(currentFiltered, isPeakNow, currentSignalGood);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            image.close();
        }
    }
}