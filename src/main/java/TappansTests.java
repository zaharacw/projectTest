import s17cs350project.planner.WaypointPlanner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by zaharacw on 5/23/17.
 */
public class TappansTests
{
    public static void main(String[] args)
    {
        String input = "1  ,2,3 \n 9,7,5 \n -1,-3, -5\n -1,-5,-9 \n   4, 6,2";
        WaypointPlanner.E_AxisNative axisA = WaypointPlanner.E_AxisNative.X_PLUS;
        WaypointPlanner.E_AxisNative axisB = WaypointPlanner.E_AxisNative.Y_PLUS;
        WaypointPlanner.E_AxisNative axisC = WaypointPlanner.E_AxisNative.Z_PLUS;
        InputStream instream = new ByteArrayInputStream(input.getBytes());
        WaypointPlanner planner = new WaypointPlanner(axisA, axisB, axisC, WaypointPlanner.E_Unit.KILOMETERS, instream);
        List<WaypointPlanner.Coordinates> coordinatesNative = planner.getCoordinates(false, WaypointPlanner.E_Unit.KILOMETERS);
        List<WaypointPlanner.Coordinates> coordinatesCanonical = planner.getCoordinates(true, WaypointPlanner.E_Unit.KILOMETERS);
        System.out.println("coordinatesNative    = " + coordinatesNative);
        System.out.println("coordinatesCanonical = " + coordinatesCanonical);
        List<Double> distancesNative = planner.calculateDistances(WaypointPlanner.E_AxisCombinationNeutral.FIRST_SECOND, false, WaypointPlanner.E_Unit.KILOMETERS);
        List<Double> distancesCanonical = planner.calculateDistances(WaypointPlanner.E_AxisCombinationNeutral.FIRST_SECOND, true, WaypointPlanner.E_Unit.KILOMETERS);
        System.out.println("distancesNative= " + distancesNative);
        System.out.println("distancesCanonical   = " + distancesCanonical);
        double distanceNative = planner.calculateDistance(WaypointPlanner.E_AxisCombinationNeutral.FIRST_SECOND, false, WaypointPlanner.E_Unit.KILOMETERS);
        double distanceCanonical = planner.calculateDistance(WaypointPlanner.E_AxisCombinationNeutral.FIRST_SECOND, true, WaypointPlanner.E_Unit.KILOMETERS);
        System.out.println("distanceNative = " + distanceNative);
        System.out.println("distanceCanonical    = " + distanceCanonical);

    }
}
