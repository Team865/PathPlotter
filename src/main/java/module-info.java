module path.planner {
    requires kotlin.stdlib;

    requires javafx.controls;
    requires wpilibj.java;
    requires wpiutil.java;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.iconli.core;

    exports ca.warp7.planner2;
    exports ca.warp7.frc2020.lib.trajectory;
    exports edu.wpi.first.hal;
    requires ntcore.java;
    requires ntcore.jni;
}