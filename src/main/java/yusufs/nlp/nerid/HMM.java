package yusufs.nlp.nerid;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Yusuf Syaifudin on 6/21/2016.
 */
public class HMMModel implements Serializable {

    private String[] label;
    private double[] start;
    private double[][] transition;
    private double[][] emission;

    public HMMModel(int states, String[] labels) {
        this.start = new double[states];
        this.transition = new double[states][states];
        this.emission = new double[states][labels.length];
        this.label = labels;
    }

    public double getStart(int i) {
        return start[i];
    }

    public void setStart(int i, double v) {
        start[i] = v;
    }

    public double getTransition(int i, int j) {
        return transition[i][j];
    }

    public void setTransition(int i, int j, double v) {
        transition[i][j] = v;
    }

    public double getEmission(int i, int j) {
        return emission[i][j];
    }

    public void setEmission(int i, int j, double v) {
        emission[i][j] = v;
    }

    public int[] mapLabels(String[] labels) {
        int[] map = new int[labels.length];
        for (int i = 0; i < labels.length; i++) {
            int value = Arrays.asList(this.label).indexOf(labels[i]);
            if(value == -1) {
                throw new IndexOutOfBoundsException("Your sequence contains value that not in initial label map.");
            }
            map[i] = value;
        }

        return map;
    }

    /**
     * Get number of states, aka, the internal dimension of the HMM
     *
     * @return int
     */
    public int count() {
        return transition.length;
    }

    public int countLabels() {
        return label.length;
    }

    public static void main(String[] args) {
        HMMModel model = new HMMModel(2, new String[]{"A", "B"});
        model.setStart(0, .85);
        model.setStart(1, .15);

        model.setTransition(0, 0, .3);
        model.setTransition(0, 1, .7);
        model.setTransition(1, 0, .1);
        model.setTransition(1, 1, .9);

        model.setEmission(0, 0, .4);
        model.setEmission(0, 1, .6);
        model.setEmission(1, 0, .5);
        model.setEmission(1, 1, .5);

        BaumWelchTrainer trainer = new BaumWelchTrainer();
        try {
            trainer.trainCycle(model, new String[]{"A", "B", "B", "A"}, 1);
            trainer.trainCycle(model, new String[]{"B", "A", "B"}, 1);
            System.out.println(model.getTransition(0, 1));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error");
            System.out.println(e.getMessage());
        }
    }

}
