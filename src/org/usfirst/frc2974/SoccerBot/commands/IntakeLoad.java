package org.usfirst.frc2974.SoccerBot.commands;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.command.Command;
import org.usfirst.frc2974.SoccerBot.Gamepad;
import org.usfirst.frc2974.SoccerBot.Robot;
import org.usfirst.frc2974.SoccerBot.RobotMap;
import org.usfirst.frc2974.SoccerBot.subsystems.AbstractIntake.ArmMovement;
import org.usfirst.frc2974.SoccerBot.subsystems.AbstractIntake.ArmPosition;
import org.usfirst.frc2974.SoccerBot.subsystems.Intake;

/**
 * Makes the intake go to the DRIBBLE position, turns the motor to load a ball
 */
class IntakeLoad extends Command {

  private final AnalogPotentiometer angleSensor = RobotMap.intakeAngleSensor;
  private boolean initialized = false;

  public IntakeLoad() {
    requires(Robot.intake);
  }

  // Called just before this Command runs the first time
  // sets the required and acceptable angles of loader
  protected void initialize() {
    initialized = false;
    Robot.intake.setMotorPower(-.35);
    if (Robot.intake.getArmPosition() == ArmPosition.UP || Robot.intake.getArmPosition() == ArmPosition.HIGH) {
      Robot.intake.setArmMovement(ArmMovement.FALL);
    } else if (Robot.intake.getArmPosition() == ArmPosition.LOW
        || Robot.intake.getArmPosition() == ArmPosition.FLAT) {
      Robot.intake.setArmMovement(ArmMovement.UP);
    }
  }

  // Called repeatedly when this Command is scheduled to run
  // controls the angle and offset of loading arm
  protected void execute() {
    if (Robot.intake.getArmPosition() == ArmPosition.DRIBBLE) {
      initialized = true;
    }
    if (initialized) {
      moveIntake();
    }
    if (Robot.oi.dribbleButton.get()) {
      new IntakeDribble().start();
    }
    if (Robot.oi.flatButton.get()) {
      new IntakeFlat().start();
    }
    if (Robot.oi.loadButton.get()) {
      initialize();
    }
  }

  private void moveIntake() {
    boolean isUpPressed = Robot.oi.xbox.getPOVButton(Gamepad.POV.N);
    boolean isFallPressed = Robot.oi.xbox.getPOVButton(Gamepad.POV.S);
    if (isUpPressed) {
      Robot.intake.setArmMovement(Intake.ArmMovement.UP);
    } else if (isFallPressed) {
      Robot.intake.setArmMovement(Intake.ArmMovement.FALL);
    } else {
      Robot.intake.setArmMovement(Intake.ArmMovement.BLOCK);
    }
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    return Robot.oi.manualButton.get();
  }

  // Called once after isFinished returns true
  protected void end() {
    Robot.intake.setMotorPower(0);
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {
    end();
  }
}
