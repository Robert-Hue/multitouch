# Project Goals #

The main idea is to provide common set algorithms, not wired to a special category of multi-touch devices:

Feature Layer:

  * [Labeling](Labeling.md) (Connected Components)
  * [Tracking](Tracking.md) (Features)
  * Physical Moments

Application Layer

  * Manipulators
  * Transform (Position, Rotation, Scale)

However, we also try to provide a minimal module for accessing cameras, because our focus is on computer vision based sensing (including FTIR). Currently, we successfully captured images utilizing Quicktime for Java and through the PointGrey FlyCapture SDK via the Java Native Interface. (Hopefully, Lib-DC-1394 will follow soon).