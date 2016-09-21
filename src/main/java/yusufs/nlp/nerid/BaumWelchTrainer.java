package yusufs.nlp.nerid;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Created by Yusuf Syaifudin on 6/20/2016.
 */
public class BaumWelchTrainer {

    /**
     * Train the given sequence multiple times
     * Train the given Hidden Markov Model n times with the specified sequence.
     *
     * @param hmm
     * @param sequences
     * @param steps
     */
    public void trainCycle(HMM hmm, String[] sequences, int steps) {
        for (int i = 0; i < steps; i++) {
            train(hmm, sequences);
        }
    }

    /**
     * Train the HMM with the given input sequence
     * @param hmm
     * @param sequences
     */
    public void train(HMM hmm, String[] sequences) {
        int[] sequence = hmm.mapLabels(sequences);
        int sequenceLength = sequence.length;
        HMM oldHMM = SerializationUtils.clone(hmm);
        int states = hmm.count();
        int labels = hmm.countLabels();

        // Calculate forward and backwards variables, first
        double[][] forward = calcForwards(oldHMM, sequence);
        double[][] backward = calcBackwards(oldHMM, sequence);

        // Reevaluate the start probabilities
        for (int i = 0; i < states; i++) {
            hmm.setStart(i, calcGamma(oldHMM, i, 0, sequence, forward, backward));
        }

        // Reevaluate the transition probabilities
        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                double numerator = 0;
                double denominator = 0;

                for (int t = 0; t <= sequenceLength - 1; t++) {
                    numerator += calcP(oldHMM, t, i, j, sequence, forward, backward);
                    denominator += calcGamma(oldHMM, i, t, sequence, forward, backward);
                }

                hmm.setTransition(i, j, divide(numerator, denominator));
            }
        }

        // Reevaluate the emission probabilities
        for (int i = 0; i < states; i++) {
            for (int k = 0; k < labels; k++) {
                double numerator = 0;
                double denominator = 0;
                for (int t = 0; t <= sequenceLength - 1; t++) {
                    double gamma = calcGamma(oldHMM, i, t, sequence, forward, backward);
                    numerator += gamma * ( (k == sequence[t]) ? 1 : 0 );
                    denominator += gamma;
                }

                hmm.setEmission(i, k, divide(numerator, denominator));
            }
        }
    }

    /**
     * Calculates the forward variables for the given sequence depending on the given HMM.
     *
     * @param hmm
     * @param sequence
     * @return
     */
    public double[][] calcForwards(HMM hmm, int[] sequence) {
        int sequenceLength = sequence.length;
        int states = hmm.count();
        double [][] forward = new double[states][sequenceLength];

        for (int i = 0; i < states; i++) {
            forward[i][0] = hmm.getStart(i) * hmm.getEmission(i, sequence[0]);
        }

        for (int t = 0; t <= sequenceLength - 2; t++) {
            for (int j = 0; j < states; j++) {
                forward[j][t + 1] = 0;
                for (int i = 0; i < states; i++) {
                    forward[j][t + 1] += forward[i][t] * hmm.getTransition(i, j);
                }
                forward[j][t + 1] *= hmm.getEmission(j, sequence[t +1 ]);
            }
        }

        return forward;
    }

    /**
     * Calculates the backward variables for the given sequence depending on the given HMM.
     *
     * @param hmm
     * @param sequence
     * @return
     */
    public double[][] calcBackwards(HMM hmm, int[] sequence) {
        int sequenceLength = sequence.length;
        int states = hmm.count();
        double[][] backward = new double[states][sequenceLength];

        // basic case
        for (int i = 0; i < states; i++) {
            backward[i][sequenceLength - 1] = 1;
        }

        // structural induction
        for (int t = sequenceLength  - 2; t >= 0; t--) {
            for (int i = 0; i < states; i++) {
                backward[i][t] = 0;
                for (int j = 0; j < states; j++) {
                    backward[i][t] += backward[j][t + 1] * hmm.getTransition(i, j) * hmm.getEmission(j, sequence[t + 1]);
                }
            }
        }

        return backward;
    }

    /**
     * Calculates the probability: P(X_t = s_i, X_t+1 = s_j | O, m)
     *
     * @param hmm
     * @param t
     * @param i
     * @param j
     * @param sequence
     * @param forward
     * @param backward
     * @return
     */
    public double calcP(HMM hmm, int t, int i, int j, int[] sequence, double[][] forward, double[][] backward) {
        int sequenceLength = sequence.length;
        int states = hmm.count();

        double numerator = 0;
        if (t == (sequenceLength - 1)) {
            numerator = forward[i][t] * hmm.getTransition(i, j);
        } else {
            numerator = forward[i][t] * hmm.getTransition(i, j)
                    * backward[j][t + 1] * hmm.getEmission(j, sequence[t + 1]);
        }

        double denominator = 0;
        for (int k = 0; k < states; k++) {
            denominator += forward[k][t] * backward[k][t];
        }

        return divide(numerator, denominator);
    }

    /**
     * Calculates gamma( i, t )
     *
     * @param hmm
     * @param t
     * @param i
     * @param sequence
     * @param forward
     * @param backward
     * @return
     */
    public double calcGamma(HMM hmm, int i, int t, int[] sequence, double[][] forward, double[][] backward) {
        int states = hmm.count();
        double numerator = forward[i][t] *  backward[i][t];
        double denominator = 0;

        for (int j = 0; j < states; j++) {
            denominator += forward[j][t] * backward[j][t];
        }

        return divide(numerator, denominator);
    }

    /**
     * Divide two floats, while 0 / 0 = 0.
     *
     * @param n
     * @param d
     * @return
     */
    public double divide(double n, double d) {
        return (n < 0.00001) ? 0.0 : n/d;
    }
}
