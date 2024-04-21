package classes;

import classes.entities.water_tanks.WaterTank;
import classes.events.WaterActionEvent;
import classes.events.WaterActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Water {
    private ArrayList<HashMap<WaterTank,Direction>> _pastSteps = new ArrayList<>(); // Commit 1 main

    private ArrayList<WaterActionListener> _listeners = new ArrayList<>();

    private final TimerTask _task = new TimerTask() {
        public void run() {
            nextStep();
        }
    };

    private final Timer _timer = new Timer("Flow timer");
    private int DELAY = 10;

    public ArrayList<HashMap<WaterTank,Direction>> getAllSteps() {
        return _pastSteps;
    }

    public void setWaterDelay(int delay) {
        DELAY = delay;
    }

    public Water(WaterTank source) {
        HashMap<WaterTank,Direction> zeroStep = new HashMap<>();
        zeroStep.put(source, null);
        _pastSteps.add(zeroStep);
    }

    public void flow() {
        _timer.scheduleAtFixedRate(_task, DELAY, DELAY);
    }

    public void stop() {
        _timer.cancel();
    }

    public void nextStep()
    {     
        HashMap<WaterTank,Direction> newStep = new HashMap<>();
        for (WaterTank waterTank : lastStep().keySet()) {
            HashMap<Direction, WaterTank> neighbours = waterTank.getConnectedWaterTanks();
            for (Direction direction : neighbours.keySet()) {
                boolean isFilled = neighbours.get(direction).fillFromDirection(direction.turnAround(), Water.this);
                if (isFilled) {
                    newStep.put(neighbours.get(direction), direction.turnAround());
                }
            }
        }

        if (newStep.isEmpty()) {
            stop();
            fireEndFlowEvent();
        } else {
            _pastSteps.add(newStep);
        }

        fireEndStepEvent();
    }

    private HashMap<WaterTank,Direction> lastStep() {
        return _pastSteps.get(_pastSteps.size()-1);
    }

    public void addListener (WaterActionListener listener) {
        _listeners.add(listener);
    }

    private void fireEndStepEvent() {
        for (WaterActionListener listener : _listeners) {
            listener.stepEnd(new WaterActionEvent(this));
        }
    }

    private void fireEndFlowEvent() {
        for (WaterActionListener listener : _listeners) {
            listener.waterEndFlow(new WaterActionEvent(this));
        }
    }

}
