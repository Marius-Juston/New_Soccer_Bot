package org.usfirst.frc2974.SoccerBot.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Subsystem;
import org.usfirst.frc2974.SoccerBot.RobotMap;
import org.usfirst.frc2974.SoccerBot.commands.NewKickSequence;

/**
 * Subsystem used by NewKickSequence
 */
public class Kicker extends Subsystem {

  private final Solenoid kickerLeft = RobotMap.kickerLeft;
  private final Solenoid kickerRight = RobotMap.kickerRight;
  private final Solenoid retract = RobotMap.retract;
  private final Solenoid latch = RobotMap.latch;
  private final DigitalInput limitSwitchForward = RobotMap.limitSwitchForward;
  private final DigitalInput limitSwitchBackward = RobotMap.limitSwitchBackward;
  private LatchPosition latchPosition;
  private boolean fullCharge = true;
  private double timer;

  public void initDefaultCommand() {
    setDefaultCommand(new NewKickSequence());
  }

  public void setRetract(boolean bool) {
    if (bool) {
      kickerLeft.set(false);
      kickerRight.set(false);
    }
    retract.set(bool);
  }

  public void setLatch(LatchPosition lp) {
    switch (lp) {
      case latched:
        latch.set(false);
        latchPosition = LatchPosition.latched;
        break;
      case unlatched:
        latch.set(true);
        latchPosition = LatchPosition.unlatched;
        break;
    }
  }

  public void setOff() {
    kickerLeft.set(false);
    kickerRight.set(false);
    retract.set(false);
    latch.set(false);
  }

  public void startCharge() {
    if (fullCharge) {
      kickerLeft.set(true);
      kickerRight.set(true);
    } else {
      kickerLeft.set(true);
      kickerRight.set(false);
    }

  }

  public void deactivateKickPistons() {
    kickerLeft.set(false);
    kickerRight.set(false);
  }

  public LatchPosition getLatchState() {
    return latchPosition;
  }

  public Position getPosition() {
    if (!limitSwitchForward.get()) {
      return Position.EXTENDED;
    }

    if (!limitSwitchBackward.get()) {
      return Position.RETRACTED;
    }

    return Position.DONT_KNOW;

  }

  public boolean getCharge() {
    return fullCharge;
  }

  public void setCharge(boolean charge) {
    fullCharge = charge;
  }

  public void startTimer() {
    timer = Timer.getFPGATimestamp();
  }

  public double getTimeSinceStart() {
    return Timer.getFPGATimestamp() - timer;
  }

  public enum LatchPosition {
    latched, unlatched
  }

  public enum Position {
    EXTENDED, RETRACTED, DONT_KNOW
  }

}
