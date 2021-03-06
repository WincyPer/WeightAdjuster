package frc.robot;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;

public class WeightAdjuster {

    // ANGLING FOR HIGHER BARS REQUIRES FOR WEIGHTSHIFTER TO GO INWARD. BEFORE SHIFTING WEIGHT (FOR THE ANGLE), MAKE SURE ELEV IS FULLY RETRACTED
    // PULLING OFF FROM LOWER BARS REQUIRES FOR WEIGHTSHIFTER TO GO OUTWARD

    //ASSUME GOING UP IS POSITIVE
    //RANGE WHEN ELEVATOR IS UP: -85 to 48 (Down to Up)
    //RANGE WHEN ELEVATOR IS DOWN: -85 to 25 (Down to Up)

    private MotorController weightAdjuster;
    private SingleChannelEncoder weightEncoder;

    private double weightSpeedUp = 0.35;               //speed going up
    private double weightSpeedDown = -0.35;            //speed going down

    private double weightMaxUp = 48;            //encoder count for the most up it can be
    private double weightMaxDown = -49;          //encoder count for the farthest down it can be
    //USED TO BE -59

    private Timer weightTimer; 

    public WeightAdjuster(MotorController WeightShifter, SingleChannelEncoder shifterEnc, Timer newWeightTimer){
        weightAdjuster = WeightShifter;
        weightEncoder = shifterEnc;
        weightTimer = newWeightTimer; 
    }

    private enum States{
        UP, DOWN, HOME, TESTING, STOP;
    }

    public States weightShifterState = States.STOP;

    public void setWeightHome(){
        weightShifterState = States.HOME;
    }

    public void setWeightUp(){
        weightShifterState = States.UP;
    }

    public void setWeightDown(){
        weightShifterState = States.DOWN;
    }

    public void setWeightStop(){
        weightShifterState = States.STOP;
    }

    public void setWeightTest(){
        weightShifterState = States.TESTING;
    }

    //BOOLS
    private boolean beforeUpLim() {
        return weightEncoder.get() <= (weightMaxUp - 10); 
    }

    public boolean beforeDownLim() {
        return ((weightEncoder.get() >= weightMaxDown) || (weightTimer.get() < 3.0)); 
    }

    private boolean beforeHomeLim() {
        return weightEncoder.get() > 0; 
    }

    private boolean afterHomeLim() {
        return weightEncoder.get() < 0; 
    }

    private boolean elevDownRange() {                      //returns true when past up range (when elev is down)
        return weightEncoder.get() >= 25; 
    }

    //METHODS
    private void weightUp(){        
        if(beforeUpLim()){     //when the encoder is greater than the encoder limit (max UP position), go up
            weightAdjuster.set(weightSpeedUp);
        }

        else{           
            weightAdjuster.set(0);
        }
    }

    public void weightUpElevDown() {              //drives weight to up limit when elevator is down
        if (elevDownRange()) {
            weightAdjuster.set(0); 
        } else {
            weightAdjuster.set(weightSpeedUp); 
        }
    }

    private void weightDown(){
        weightTimer.start();
        if(beforeDownLim()){       
        weightAdjuster.set(weightSpeedDown);
        }

        else{
            weightAdjuster.set(0);
            weightTimer.stop();
        }
    }

    private void weightHome(){
        if(afterHomeLim()){      //when the weight is down, adjust the weight inwards
            weightAdjuster.set(weightSpeedUp);
        }

        else if(beforeHomeLim()){      //when the weight is up, adjust the weight outwards
            weightAdjuster.set(weightSpeedDown);
        }

        else{
            weightAdjuster.set(0);
        }
    }

    private void stop(){
        weightAdjuster.set(0);
    }

    public void manualWeight(double speed){     //manually control the weight adjuster
        weightAdjuster.set(speed);
    }

    public void resetEncoder(){
        weightEncoder.reset();
    }

    public void resetTimer(){          //USE THIS IN HANG CLASS FOR RESETTING TIMER
        weightTimer.reset();
    }

    public void testing (){
        //EMPTY CODE FOR TESTING
    }

    public void run(){

        SmartDashboard.putNumber("WEIGHT ENCODER", weightEncoder.get());
        SmartDashboard.putNumber("SPEED", weightAdjuster.get()); 
        SmartDashboard.putBoolean("BEFORE UP LIM", beforeUpLim());
        SmartDashboard.putBoolean("BEFORE DOWN LIM", beforeDownLim());
        SmartDashboard.putBoolean("BEFORE HOME LIM", beforeHomeLim()); 
        SmartDashboard.putBoolean("AFTER HOME LIM", afterHomeLim()); 
        SmartDashboard.putString("WEIGHT STATE", weightShifterState.toString()); 
        
        switch(weightShifterState){

            case UP:
            weightUp();
            break;

            case DOWN:
            weightDown();
            break;

            case HOME:
            weightHome();
            break;

            case TESTING:
            testing();
            break;

            case STOP:
            stop();
            break;

        }

    }
}
