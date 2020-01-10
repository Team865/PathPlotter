module path.planner {
    requires kotlin.stdlib;

    requires javafx.controls;
    requires wpilibj.java;
    requires wpiutil.java;
//    requires ejml.combined;

    exports ca.warp7.planner2;
    exports ca.warp7.frc2020.lib.trajectory;
    exports edu.wpi.first.hal;
}