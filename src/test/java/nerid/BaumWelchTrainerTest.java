package nerid;

import static org.junit.Assert.*;
import org.junit.Test;
import yusufs.nlp.nerid.BaumWelchTrainer;
import yusufs.nlp.nerid.HMM;

/**
 * Created by yusuf on 21/09/16.
 */
public class BaumWelchTrainerTest {

    @Test
    public void testBaumWelchTrainer() {
        HMM model = new HMM(2, new String[]{"A", "B"});
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
        trainer.trainCycle(model, new String[]{"A", "B", "B", "A"}, 1);

        double expected1 = 0.67232940721228;
        double actual1 = model.getTransition(0, 1);
        assertEquals(expected1, actual1, Math.abs(expected1 - actual1));

        // do training again
        trainer.trainCycle(model, new String[]{"B", "A", "B"}, 1);
        double expected2 = 0.6503368443181307;
        double actual2 = model.getTransition(0, 1);
        assertEquals(expected2, actual2, Math.abs(expected2 - actual2));
    }
}
