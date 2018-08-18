package org.usfirst.frc2974.SoccerBot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc2974.SoccerBot.Robot;
import org.usfirst.frc2974.SoccerBot.subsystems.AbstractIntake.ArmPosition;
import org.usfirst.frc2974.SoccerBot.subsystems.Kicker.LatchPosition;
import org.usfirst.frc2974.SoccerBot.subsystems.Kicker.Position;

/**
 * 2017 arm sequencing, defaults to be UP and latched
 */
public class NewKickSequence extends Command {

  final private static double TIMEOUT = 3;

  private State state;

  public NewKickSequence() {
    requires(Robot.kicker);
  }

  // Called just before this Command runs the first time
  protected void initialize() {
    state = State.INIT;
    state.init(this);
  }

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    State newState = state.run(this);
    if (newState != state) {
      state = newState;
      state.init(this);
    }
    SmartDashboard.putString("Kicker state", state.toString());
    SmartDashboard.putString("Intake position", Robot.intake.getArmPosition().toString());
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    return false;
  }

  // Called once after isFinished returns true
  protected void end() {
    Robot.kicker.setOff();
    Robot.intake.endFlat();
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {
    end();
  }

  public enum State {
    /**
     * Starts retracting the arm
     */
    INIT {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.deactivateKickPistons();
        Robot.kicker.setLatch(LatchPosition.unlatched);
        Robot.kicker.setRetract(true);
      }

      @Override
      public State run(NewKickSequence nks) {
        return RETRACTING;
      }
    },
    RETRACTING {
      @Override
      public State run(NewKickSequence nks) {
        if (Robot.kicker.getPosition() == Position.RETRACTED) {
          return LATCH_PAUSE;
        } else {
          return INIT;
        }

      }
    },
    /**
     * Waits a moment to latch, so the arm can have time to get all the way UP
     */
    LATCH_PAUSE {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.startTimer();
      }

      @Override
      public State run(NewKickSequence nks) {
        if (Robot.kicker.getTimeSinceStart() > .5) {
          Robot.kicker.setLatch(LatchPosition.latched);
        }
        if (Robot.kicker.getTimeSinceStart() > .75) {
          Robot.kicker.setRetract(false);
          return RESTING;
        }
        return this;
      }

    },
    /**
     * Ideal state
     */
    RESTING {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.deactivateKickPistons();
        Robot.kicker.startTimer();
      }

      @Override
      public State run(NewKickSequence nks) {
        if (Robot.oi.readyButton.get() && Robot.kicker.getTimeSinceStart() > 0.5) {
          return PREKICK;
        }
        if (Robot.oi.adjustArm.get()) {
          return ADJUST_ARM;
        }
        if (Robot.kicker.getPosition() != Position.RETRACTED) {
          return INIT;
        }
        return this;
      }
    },
    /**
     * Primes the arm by providing pressure
     */
    PREKICK {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.startCharge();
        Robot.intake.startFlat();
      }

      @Override
      public State run(NewKickSequence nks) {
        if (!Robot.oi.readyButton.get()) {
          return RESTING;
        }

        if (Robot.oi.kickButton.get() && Robot.intake.getArmPosition() == ArmPosition.FLAT) {
          return KICK;
        }
        return this;
      }
    },
    KICK {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.startTimer();
      }

      @Override
      public State run(NewKickSequence nks) {
        if (!Robot.oi.readyButton.get()) {
          return RESTING;
        }
        Robot.kicker.setLatch(LatchPosition.unlatched);
        return KICKING;
      }
    },
    KICKING {
      @Override
      public State run(NewKickSequence nks) {
        if (!Robot.oi.readyButton.get()) {
          return INIT;
        }
        if (Robot.kicker.getTimeSinceStart() > TIMEOUT || Robot.kicker.getPosition() == Position.EXTENDED) {
          return INIT;
        }
        return this;
      }
    },
    /**
     * Drops the arm while adjustArm is depressed, in case the latch is pressing against the arm
     */
    ADJUST_ARM {
      @Override
      public void init(NewKickSequence nks) {
        Robot.kicker.setLatch(LatchPosition.unlatched);
      }

      @Override
      public State run(NewKickSequence nks) {
        if (!Robot.oi.adjustArm.get()) {
          return INIT;
        }
        return this;
      }
    };

    State run(NewKickSequence nks) {
      return this;
    }

    void init(NewKickSequence nks) {

    }
  }

}
