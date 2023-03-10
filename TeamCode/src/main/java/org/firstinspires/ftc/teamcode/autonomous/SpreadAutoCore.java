
package org.firstinspires.ftc.teamcode.autonomous;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.autonomous.imagecv.AprilTagDetectionPipeline;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.robot.AutoBrainSTEMRobot;
import org.firstinspires.ftc.teamcode.robot.Constants;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpreadAutoCore extends LinearOpMode {
    private final AutoOrientation side;
    private Map stateMap;

    private Vector2d endParking;


    // Locations - Right /////////////////////////////////////////////////////////////////////
    Pose2d startPosition = new Pose2d(-34, -64, Math.toRadians(90));
    Vector2d depositPreloadonLow = new Vector2d(-34, -48);
    Vector2d collectionApporach = new Vector2d(-36, -20);
    Pose2d highPoleDepositingPosition = new Pose2d(-23.25, -12.5, Math.toRadians(0));
    Pose2d highPoleDepositingPosition2 = new Pose2d(-24.3, -11.5, Math.toRadians(0));
    Pose2d lowPoleDepositingPosition = new Pose2d(-47.5, -11.5, Math.toRadians(0));
    Pose2d secondHighPoleDepositingPosition = new Pose2d(0.01, -11.5, Math.toRadians(0));
    Pose2d highPoleDepositingIntermediatePoint = new Pose2d(highPoleDepositingPosition.getX() - 2, highPoleDepositingPosition.getY(), highPoleDepositingPosition.getHeading());
    Pose2d collectConesPosition = new Pose2d(-55.25, -12.5, Math.toRadians(0));
    Pose2d depositOnHighPole1approach = new Pose2d(-35, -12, Math.toRadians(0));
    Pose2d depositOnHighPole1 = new Pose2d(-24, -12, Math.toRadians(0));
    Pose2d depositOnHighPole2 = new Pose2d(-25, -12, Math.toRadians(0));
    Pose2d depositPreloadForward = new Pose2d(-62, -36, Math.toRadians(100));
    Vector2d approachVector = new Vector2d(-60, -24);
    double approachHeading = Math.toRadians(90);
    Vector2d depositPreLoadForwardVector = new Vector2d(-57, -13.5);
    double depositPreLoadForwardHeading = Math.toRadians(100);
    String turretPickupPosition;
    String turretDeliveryPosition1;
    String turretDeliveryPosition2;


    private int initialTurn = -90;

    private Vector2d parking3 = new Vector2d(-12, -12.5);
    private Vector2d parking2 = new Vector2d(-36, -12.5);
    private Vector2d parking1 = new Vector2d(-60, -12.5);

    private int initialTangent = -80;
    int collectConeTangent = 90;
    private int initialApproachTangent = 90;
    private int highPoleDepositingPositionTangent = 90;
    private int depositPreloadSpline2Tangent = 125;
    private String extensionCollectGoTo;
    private String lowTurretDeliveryPosition;


    // Async Vars /////////////////////////////////////////////////////////////////////
    private boolean step1 = false;
    private boolean step2 = false;
    private boolean step3 = false;
    private boolean step4 = false;
    private boolean step4a = false;
    private boolean step5 = false;
    private boolean step5a = false;

    private int parking;

    private ArrayList liftCollectionHeights;
    Constants constants = new Constants();

    TrajectorySequence autoTrajectorySequence;


    // Open CV //////////////////////////////////////////////////////////////////////////
    private ParkingLocation location = ParkingLocation.LEFT;
    public OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;
    static final double FEET_PER_METER = 3.28084;
    public int Ending_Location = 1;
    double fx = 578.272;
    double fy = 1000;
    double cx = 100;
    double cy = 221.506;
    double tagsize = 0.00037;
    int LEFT = 1;
    int MIDDLE = 2;
    int RIGHT = 3;
    AprilTagDetection tagOfInterest = null;


    private enum ParkingLocation {
        LEFT, MID, RIGHT
    }

    public enum AutoOrientation {
        RIGHT, LEFT
    }

    public SpreadAutoCore(AutoOrientation side) {
        this.side = side;
        switch (side) {
            case LEFT:
                initialTangent = -80;
                collectConeTangent = 90;
                initialApproachTangent = 90;
                highPoleDepositingPositionTangent = 0;
                depositPreloadSpline2Tangent = 25;
                endParking = new Vector2d(parking2.getX(), parking2.getY());
                break;
            case RIGHT:
                depositPreloadonLow = new Vector2d(depositPreloadonLow.getX(), -depositPreloadonLow.getY());
                initialTangent = 80;
                collectConeTangent = -90;
                initialApproachTangent = -90;
                highPoleDepositingPositionTangent = 0;
                depositPreloadSpline2Tangent = -115;
                startPosition = new Pose2d(startPosition.getX(), -startPosition.getY(), Math.toRadians(-90));
                initialTurn = 90;
                collectionApporach = new Vector2d(collectionApporach.getX(), -collectionApporach.getY());
                highPoleDepositingPosition = new Pose2d(highPoleDepositingPosition.getX(), -highPoleDepositingPosition.getY(), Math.toRadians(180));
                highPoleDepositingPosition2 = new Pose2d(highPoleDepositingPosition2.getX(), -highPoleDepositingPosition2.getY(), Math.toRadians(180));
                lowPoleDepositingPosition = new Pose2d(lowPoleDepositingPosition.getX(), -lowPoleDepositingPosition.getY(), Math.toRadians(180));
                highPoleDepositingIntermediatePoint = new Pose2d(highPoleDepositingPosition.getX() + 2, highPoleDepositingPosition.getY(), highPoleDepositingPosition.getHeading());
                depositPreloadForward = new Pose2d(depositPreloadForward.getX(), -depositPreloadForward.getY(), -depositPreloadForward.getHeading());
                depositPreLoadForwardVector = new Vector2d(depositPreLoadForwardVector.getX(), -depositPreLoadForwardVector.getY());
                depositPreLoadForwardHeading = Math.toRadians(-100);
                approachVector = new Vector2d(approachVector.getX(), -approachVector.getY());
                approachHeading = Math.toRadians(-90);
                collectConesPosition = new Pose2d(collectConesPosition.getX(), -collectConesPosition.getY(), Math.toRadians(180));
                depositOnHighPole1approach = new Pose2d(depositOnHighPole1approach.getX(), -depositOnHighPole1approach.getY(), Math.toRadians(180));
                depositOnHighPole1 = new Pose2d(depositOnHighPole1.getX(), -depositOnHighPole1.getY(), Math.toRadians(180));
                depositOnHighPole2 = new Pose2d(depositOnHighPole2.getX(), -depositOnHighPole2.getY(), Math.toRadians(180));
                secondHighPoleDepositingPosition = new Pose2d(secondHighPoleDepositingPosition.getX(), -secondHighPoleDepositingPosition.getY(), Math.toRadians(180));
                parking1 = new Vector2d(-12, 12.5);
                parking2 = new Vector2d(-36, 12.5);
                parking3 = new Vector2d(-60, 12.5);
                endParking = new Vector2d(-36, 12.5);
                break;
        }
    }


    @Override
    public void runOpMode() throws InterruptedException {

        // Timers ////////////////////////////////////////////////////////////////////////
        ElapsedTime runTime = new ElapsedTime();
        ElapsedTime totalTime = new ElapsedTime();

        // Hardwhare ///////////////////////////////////////////////////////////////////
        SampleMecanumDrive drive = new SampleMecanumDrive(this.hardwareMap);
        this.stateMap = new HashMap<String, String>() {{
        }};
        AutoBrainSTEMRobot robot = new AutoBrainSTEMRobot(this.hardwareMap, this.telemetry, this.stateMap, true);

        switch (side) {
            case LEFT:
                extensionCollectGoTo = robot.arm.AUTO_EXTENSION_COLLECT_LEFT;
                turretPickupPosition = robot.turret.LEFT_PICKUP_AUTO;
                turretDeliveryPosition1 = robot.turret.RIGHT_POSITION;
                turretDeliveryPosition2 = robot.turret.LEFT_POSITION;
                lowTurretDeliveryPosition = robot.turret.LEFT_POSITION;

                break;
            case RIGHT:
                lowTurretDeliveryPosition = robot.turret.RIGHT_POSITION;
                extensionCollectGoTo = robot.arm.AUTO_EXTENSION_COLLECT_RIGHT;
                turretPickupPosition = robot.turret.RIGHT_PICKUP_AUTO;
                turretDeliveryPosition1 = robot.turret.LEFT_POSITION; //this should be left
                turretDeliveryPosition2 = robot.turret.RIGHT_POSITION;
                break;
        }

        // State Map ////////////////////////////////////////////////////////////////
        this.stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
        this.stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_GROUND);
        this.stateMap.put(robot.lift.LIFT_SUBHEIGHT, robot.lift.APPROACH_HEIGHT);
        this.stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);

        // Open CV //////////////////////////////////////////////////////////////////
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam-2"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(1280, 720, OpenCvCameraRotation.UPSIDE_DOWN);
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        telemetry.setMsTransmissionInterval(50);
        robot.grabber.close();
        while (!this.opModeIsActive() && !this.isStopRequested()) {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            if (currentDetections.size() != 0) {
                boolean tagFound = false;

                for (AprilTagDetection tag : currentDetections) {

                    if (tag.id == MIDDLE) {
                        tagOfInterest = tag;
                        location = ParkingLocation.MID;
                        parking = 2;
                        tagFound = true;
                        telemetry.addData("Open CV :", "Mid");
                        telemetry.update();
                        endParking = new Vector2d(parking2.getX(), parking2.getY());
                        break;

                    } else if (tag.id == RIGHT) {
                        tagOfInterest = tag;
                        location = ParkingLocation.RIGHT;
                        parking = 3;
                        tagFound = true;
                        telemetry.addData("Open CV :", "Right");
                        telemetry.update();
                        endParking = new Vector2d(parking3.getX(), parking3.getY());
                        break;

                    } else {
                        tagOfInterest = tag;
                        location = ParkingLocation.LEFT;
                        tagFound = true;
                        parking = 1;
                        telemetry.addData("Open CV :", "Left");
                        telemetry.update();
                        endParking = new Vector2d(parking1.getX(), parking1.getY());
                        break;

                    }
                }


            }
        }
        this.waitForStart();
        camera.closeCameraDevice();
        drive.setPoseEstimate(startPosition);
        stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
        stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
        stateMap.put(constants.CYCLE_LIFT_DOWN, constants.STATE_NOT_STARTED);
        stateMap.put(constants.CYCLE_GRABBER, constants.STATE_NOT_STARTED);
        stateMap.put(constants.CYCLE_LIFT_UP, constants.STATE_NOT_STARTED);
        stateMap.put(constants.CONE_CYCLE, constants.STATE_NOT_STARTED);
        robot.updateSystems();

        totalTime.reset();


        TrajectorySequence deliverPreload = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                .setTangent(initialTangent)
                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_4);
                })

                // PRE LOAD
                .splineToConstantHeading(depositPreloadonLow, Math.toRadians(initialApproachTangent))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition1);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);

                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_5);
                })
                .waitSeconds(1)

                // COLLECT CONE
                .splineToConstantHeading(collectionApporach, Math.toRadians(-100))
                .splineToSplineHeading(collectConesPosition, Math.toRadians(180))
                .UNSTABLE_addTemporalMarkerOffset(-0.7, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, extensionCollectGoTo);
                })

                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .waitSeconds(0.1)

                //FIRST CONE LOW POLE

                .setReversed(true)
                .splineToConstantHeading(new Vector2d(lowPoleDepositingPosition.getX(), lowPoleDepositingPosition.getY()), Math.toRadians(180))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition2);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_4);
                })


                .waitSeconds(1)

                .splineToConstantHeading(new Vector2d(collectConesPosition.getX(), collectConesPosition.getY()), Math.toRadians(180))
                .UNSTABLE_addTemporalMarkerOffset(-0.7, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, extensionCollectGoTo);
                })

                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .waitSeconds(0.1)

                //SECOND CONE MEDIUM POLE

                .splineToSplineHeading(highPoleDepositingPosition, Math.toRadians(highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(50, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(50))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_MEDIUM);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition2);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_3);
                })


                .waitSeconds(1)


                //CONE COLLECTION

                .setReversed(false)
                .splineToConstantHeading(new Vector2d(collectConesPosition.getX(), collectConesPosition.getY()), Math.toRadians(180 - highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(60, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(60))

                .UNSTABLE_addTemporalMarkerOffset(-0.7, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, extensionCollectGoTo);
                })

                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .waitSeconds(0.1)
                .setReversed(true)

                //THIRD CONE HIGH POLE

                .splineToConstantHeading(new Vector2d(highPoleDepositingPosition2.getX(), highPoleDepositingPosition2.getY()),
                        Math.toRadians(highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(45, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(45))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_HIGH);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition1);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_2);
                })


                .waitSeconds(1)


//                                CONE COLLECTION

                .setReversed(false)
                .splineToConstantHeading(new Vector2d(collectConesPosition.getX(), collectConesPosition.getY()), Math.toRadians(180 - highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(60, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(60))

                .UNSTABLE_addTemporalMarkerOffset(-0.7, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, extensionCollectGoTo);
                })

                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .waitSeconds(0.1)
                .setReversed(true)

                //FOURTH CONE HIGH POLE

                .splineToConstantHeading(new Vector2d(secondHighPoleDepositingPosition.getX(), secondHighPoleDepositingPosition.getY()),
                        Math.toRadians(180 - highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(40, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(40))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_HIGH);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition2);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.STACK_1);
                })


                .waitSeconds(1)


                .waitSeconds(1)

                //CONE COLLECTION

                .setReversed(false)
                .splineToConstantHeading(new Vector2d(collectConesPosition.getX(), collectConesPosition.getY()), Math.toRadians(highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(40, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(40))

                .UNSTABLE_addTemporalMarkerOffset(-0.7, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, extensionCollectGoTo);
                })

                .UNSTABLE_addTemporalMarkerOffset(-0.1, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.CLOSED_STATE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.1, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_LOW);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.3, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .waitSeconds(0.1)
                .setReversed(true)

                //FIFTH CONE DEPOSITING

                .splineToConstantHeading(new Vector2d(secondHighPoleDepositingPosition.getX(), secondHighPoleDepositingPosition.getY()),
                        Math.toRadians(highPoleDepositingPositionTangent),
                        SampleMecanumDrive.getVelocityConstraint(40, 4.5, 12),
                        SampleMecanumDrive.getAccelerationConstraint(40))
                .UNSTABLE_addTemporalMarkerOffset(-0.75, () -> {
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_HIGH);
                    robot.lift.setSubheight(0);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, turretDeliveryPosition1);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.0, () -> {
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_SIDE_EXTENDED_AUTO);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.4, () -> {
                    robot.lift.setSubheight(0.7);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.7, () -> {
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.FULLY_OPEN);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.8, () -> {
                    stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                    stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                })
                .UNSTABLE_addTemporalMarkerOffset(0.9, () -> {
                    robot.lift.setSubheight(0);
                    stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                    stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POSITION_GROUND);
                })
                .build();

        drive.followTrajectorySequenceAsync(deliverPreload);


        while (opModeIsActive()) {

            if (totalTime.seconds() < 28.6) {

                drive.update();
                robot.updateSystems();
                telemetry.update();
                telemetry.addData("Lift state", stateMap.get(robot.lift.LIFT_SYSTEM_NAME));
            } else {

                stateMap.put(robot.grabber.SYSTEM_NAME, robot.grabber.OPEN_STATE);
                stateMap.put(robot.turret.SYSTEM_NAME, robot.turret.CENTER_POSITION);
                stateMap.put(robot.arm.SYSTEM_NAME, robot.arm.DEFAULT_VALUE);
                stateMap.put(robot.lift.LIFT_SYSTEM_NAME, robot.lift.LIFT_POLE_GROUND);
                stateMap.put(robot.lift.LIFT_SUBHEIGHT, robot.lift.APPROACH_HEIGHT);
                robot.updateSystems();
                Trajectory parkingTrajectory = drive.trajectoryBuilder(drive.getPoseEstimate())
                        .lineToConstantHeading(endParking)
                        .build();
                drive.followTrajectory(parkingTrajectory);


            }
        }


    }
}


