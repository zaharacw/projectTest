package s17cs350project.planner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by zaharacw on 5/17/17.
 *
 * Defines a planning tool for calculating linear distances over a path of three-dimensional coordinates. It accepts an
 * input stream of comma-delimited coordinate triples with one triple per line. This native coordinate system of
 * triples can be any permutation of the components (x,y,z), with each axis independently increasing in either
 * direction. The canonical coordinate system maps each component and direction to (A,B,C).
 *
 * The canonical coordinate system is always in meters. The native one can be in feet, meters, miles, or kilometers.
 *
 * See the writeup for more details and examples.
 *
 * Throw a RuntimeException for any errors.
 */
public class WaypointPlanner
{
    private static final double[][] conversionToFrom =
            {
                    //Feet, Kilos, Meters, Miles
                    {1, 3280.84, 3.28084, 5280},
                    {0.0003048, 1, 0.001, 1.60934},
                    {0.3048, 1000, 1, 1609.34},
                    {0.000189394, 0.621371, 0.000621371, 1}

            };

    private E_AxisNative axisA;
    private E_AxisNative axisB;
    private E_AxisNative axisC;
    private E_Unit unitNative;
    private List<Coordinates> coordinatesNative;
    private List<Coordinates> coordinatesCanonical;

    /**
     * Creates a planner. It defines the axes and reads the coordinates. [3]
     *
     * @param axisA         the definition of how to interpret the A axis
     * @param axisB         the definition of how to interpret the B axis
     * @param axisC         the definition of how to interpret the C axis
     * @param unitNative    the unit of the native coordinate system
     * @param instream      the input stream containing the coordinates
     */
    public WaypointPlanner(WaypointPlanner.E_AxisNative axisA,
                           WaypointPlanner.E_AxisNative axisB,
                           WaypointPlanner.E_AxisNative axisC,
                           WaypointPlanner.E_Unit unitNative,
                           java.io.InputStream instream)
    {
        if (axisA == null || axisB == null || axisC == null)
        {
            throw new RuntimeException("Attempting to instantiate WaypointPlanner with null axis");
        }

        if (unitNative == null)
        {
            throw new RuntimeException("Attempting to instantiate WaypointPlanner with null units");
        }

        if (instream == null)
        {
            throw new RuntimeException("Attempting to instantiate WaypointPlanner with null input stream");
        }

        this.axisA = axisA;
        this.axisB = axisB;
        this.axisC = axisC;
        this.unitNative = unitNative;

        this.coordinatesNative = new ArrayList<>();
        this.coordinatesCanonical = new ArrayList<>();

        this.processInput(instream);

        //TODO TEST!
    }

    private void processInput(InputStream instream)
    {
        InputStreamReader isr = new InputStreamReader(instream);
        BufferedReader br = new BufferedReader(isr);

        double x;
        double y;
        double z;
        String line;
        boolean hasLines = true;

        while(hasLines)
        {
            try
            {
                line = br.readLine();
                if (line == null)
                {
                    hasLines = false;
                    break;
                }
                line = line.trim();

                if(!line.isEmpty())
                {
                    String[] tokens = line.split(",");

                    if(tokens.length != 3)
                    {
                        throw new RuntimeException("Invalid input format, must be 3 numbers per line");
                    }

                    try
                    {
                        x = Double.parseDouble(tokens[0].trim());
                        y = Double.parseDouble(tokens[1].trim());
                        z = Double.parseDouble(tokens[2].trim());

                        this.coordinatesNative.add(new Coordinates(x, y, z));
                        this.coordinatesCanonical.add(convertToCanonical(x, y, z));

                    }catch(NumberFormatException e)
                    {
                        throw new RuntimeException("Invalid input format, non numeric inputs");
                    }
                }

            }catch(IOException e)
            {
                hasLines = false;
            }
        }

        System.out.println("TEST: Finished Processing"); //----------------------------
    }

    private Coordinates convertToCanonical(double x, double y, double z)
    {
        double a;
        double b;
        double c;
        double conversion = conversionToFrom[E_Unit.METERS.ordinal()][this.unitNative.ordinal()];

        switch(this.axisA)
        {
            case X_PLUS:    a = x;
                            break;
            case X_MINUS:   a = -x;
                            break;
            case Y_PLUS:    a = y;
                            break;
            case Y_MINUS:   a = -y;
                            break;
            case Z_PLUS:    a = z;
                            break;
            case Z_MINUS:   a = -z;
                            break;
            default:    throw new RuntimeException("axisA not set");
        }

        switch(this.axisB)
        {
            case X_PLUS:    b = x;
                            break;
            case X_MINUS:   b = -x;
                            break;
            case Y_PLUS:    b = y;
                            break;
            case Y_MINUS:   b = -y;
                            break;
            case Z_PLUS:    b = z;
                            break;
            case Z_MINUS:   b = -z;
                            break;
            default:    throw new RuntimeException("axisB not set");
        }

        switch(this.axisC)
        {
            case X_PLUS:    c = x;
                            break;
            case X_MINUS:   c = -x;
                            break;
            case Y_PLUS:    c = y;
                            break;
            case Y_MINUS:   c = -y;
                            break;
            case Z_PLUS:    c = z;
                            break;
            case Z_MINUS:   c = -z;
                            break;
            default:    throw new RuntimeException("axisC not set");
        }

        return new Coordinates(a * conversion, b * conversion, c * conversion);
    }

    /**
     * Calculates the total distance along the path of coordinates. [5]
     *
     * @param axes                      the axes of the coordinates to account for in calculating the distance
     * @param isCanonicalElseNative     whether to use the canonical order of the components in each coordinate triple
     *                                  as defined in the constructor or the original native order from the input stream
     * @param unit                      the unit of the distances
     * @return                          the total distance
     */
    public double calculateDistance(E_AxisCombinationNeutral axes,
                                    boolean isCanonicalElseNative,
                                    E_Unit unit)
    {
        //TODO Test
        List<Double> distances = calculateDistances(axes, isCanonicalElseNative, unit);
        return distances.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Calculates the intermediate distances between coordinates along the path of coordinates. [30]
     *
     * @param axes                      the axes of the coordinates to account for in calculating the distance
     * @param isCanonicalElseNative     whether to use the canonical order of the components in each coordinate triple
     *                                  as defined in the constructor or the original native order from the input stream
     * @param unit                      the unit of the distances
     * @return                          the total distance
     */
    public List<Double> calculateDistances(E_AxisCombinationNeutral axes,
                                           boolean isCanonicalElseNative,
                                           E_Unit unit)
    {
        //TODO Test

        List<Double> distances = new ArrayList<>();
        List<Coordinates> coords = null;
        E_Unit storedUnits;

        if (isCanonicalElseNative)
        {
            coords = this.coordinatesCanonical;
            storedUnits = E_Unit.METERS;
        }
        else
        {
            coords = this.coordinatesNative;
            storedUnits = this.getUnitNative();
        }

        double conversion = conversionToFrom[unit.ordinal()][storedUnits.ordinal()];

        for (int i = 1; i < coords.size(); i++)
        {
            Coordinates beg = coords.get(i - 1);
            Coordinates end = coords.get(i);

            if (beg == null || end == null)
            {
                throw new RuntimeException("Attempting to calculate distance between null nodes.");
            }

            switch(axes)
            {
                case FIRST: distances.add(calcFirst(beg, end) * conversion);
                    break;
                case SECOND: distances.add(calcSecond(beg, end) * conversion);
                    break;
                case THIRD: distances.add(calcThird(beg, end) * conversion);
                    break;
                case FIRST_SECOND: distances.add(calcFirstSecond(beg, end) * conversion);
                    break;
                case FIRST_THIRD: distances.add(calcFirstThird(beg, end) * conversion);
                    break;
                case SECOND_THIRD: distances.add(calcSecondThird(beg, end) * conversion);
                    break;
                case FIRST_SECOND_THIRD: distances.add(calcFirstSecondThird(beg, end) * conversion);
                    break;
            }
        }

        return distances;
    }

    private Double calcFirst(Coordinates beg, Coordinates end)
    {
        return Math.abs(end.first - beg.first);
    }

    private Double calcSecond(Coordinates beg, Coordinates end)
    {
        return Math.abs(end.second - beg.second);
    }

    private Double calcThird(Coordinates beg, Coordinates end)
    {
        return Math.abs(end.third - beg.third);
    }

    private Double calcFirstSecond(Coordinates beg, Coordinates end)
    {
        return Math.sqrt(Math.pow(end.first - beg.first, 2) + Math.pow(end.second - beg.second, 2));
    }

    private Double calcFirstThird(Coordinates beg, Coordinates end)
    {
        return Math.sqrt(Math.pow(end.first - beg.first, 2) + Math.pow(end.third - beg.third, 2));
    }

    private Double calcSecondThird(Coordinates beg, Coordinates end)
    {
        return Math.sqrt(Math.pow(end.third - beg.third, 2) + Math.pow(end.second - beg.second, 2));
    }

    private Double calcFirstSecondThird(Coordinates beg, Coordinates end)
    {
        return Math.sqrt(
                Math.pow(end.first - beg.first, 2) +
                Math.pow(end.second - beg.second, 2) +
                Math.pow(end.third - beg.third, 2));
    }



    /**
     * Gets the definition of how to interpret the A axis. [1]
     *
     * @return  the definition
     */
    public E_AxisNative getAxisA()
    {
        return this.axisA;
    }

    /**
     * Gets the definition of how to interpret the B axis. [1]
     *
     * @return  the definition
     */
    public E_AxisNative getAxisB()
    {
        return this.axisB;
    }

    /**
     * Gets the definition of how to interpret the C axis. [1]
     *
     * @return  the definition
     */
    public E_AxisNative getAxisC()
    {
        return this.axisC;
    }

    /**
     * Returns the coordinates read from the input stream. [5]
     *
     * @param isCanonicalElseNative whether to use the canonical order of the components in each coordinate triple as
     *                              defined in the constructor or the original native order from the input stream
     * @param unit                  the unit of the coordinates
     * @return                      the coordinates
     */
    public List<Coordinates> getCoordinates(boolean isCanonicalElseNative,
                                            E_Unit unit)
    {
        List<Coordinates> result = new ArrayList<>();
        List<Coordinates> coords = null;
        E_Unit storedUnits;

        if (isCanonicalElseNative)
        {
            coords = this.coordinatesCanonical;
            storedUnits = E_Unit.METERS;
        }
        else
        {
            coords = this.coordinatesNative;
            storedUnits = this.getUnitNative();
        }

        double conversion = conversionToFrom[unit.ordinal()][storedUnits.ordinal()];

        for (Coordinates c : coords)
        {
            if (c == null)
            {
                //TODO ASK Tappan what he wants
            }
            result.add(new Coordinates(
                    c.first * conversion,
                    c.second * conversion,
                    c.third * conversion));
        }

        return result;
        //TODO TEST!!!!n
    }

    /**
     * Gets the unit of the native coordinate system. [1]
     *
     * @return the unit
     */
    public E_Unit getUnitNative()
    {
        return this.unitNative;
    }

    /**
     * Defines a generic triple of coordinates defined as (first,second,third). Each word corresponds to the position,
     * but the interpretation depends on which axis is mapped to it; for example, (z,y,x) or (A,B,C).
     */
    public static class Coordinates
    {
        private double first;
        private double second;
        private double third;

        /**
         * creates a triple
         * @param first     first element
         * @param second    second element
         * @param third     third element
         */
        public Coordinates(double first,
                           double second,
                           double third)
        {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        /**
         * Outputs the triple in the form (<i>first</i> <i>second</i> <i>third</i>). [1]
         *
         * @return  the string representation of the Coordinates
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(this.first);
            sb.append(" ");
            sb.append(this.second);
            sb.append(" ");
            sb.append(this.third);
            sb.append(")");

            return sb.toString();
        }
    }

    /**
     * Defines which components of a coordinate triple to use in distance calculations. [7]
     */
    public static enum E_AxisCombinationNeutral
    {
        /**
         * use the first component only; e.g., (x2 - x1)
         */
        FIRST,

        /**
         * use the second component only; e.g., (y2 - y1)
         */
        SECOND,

        /**
         * use the third component only; e.g., (z2 - z1)
         */
        THIRD,

        /**
         * use the first and second components only; e.g., ((x2,y2) - (x1,y1))
         */
        FIRST_SECOND,

        /**
         * use the first and third components only
         */
        FIRST_THIRD,

        /**
         * use the second and third components only
         */
        SECOND_THIRD,

        /**
         * use all components; e.g., ((x2,y2,z2) - (x1,y1,z1))
         */
        FIRST_SECOND_THIRD
    }

    /**
     * Defines how to interpret a lettered axis; i.e., axisA, axisB, or axisC. [6]
     */
    public static enum E_AxisNative
    {
        /**
         * interprets the lettered axis as x with negative values going in the lettered positive direction
         */
        X_MINUS,

        /**
         * interprets the lettered axis as x with positive values going in the lettered positive direction
         */
        X_PLUS,

        /**
         * interprets the lettered axis as y with negative values going in the lettered positive direction
         */
        Y_MINUS,

        /**
         * interprets the lettered axis as y with positive values going in the lettered positive direction
         */
        Y_PLUS,

        /**
         * interprets the lettered axis as z with negative values going in the lettered positive direction
         */
        Z_MINUS,

        /**
         * interprets the lettered axis as z with positive values going in the lettered positive direction
         */
        Z_PLUS

    }

    /**
     * Defines the unit of the native coordinate system. [4]
     */
    public static enum E_Unit
    {
        /**
         * feet
         */
        FEET,

        /**
         * kilometers
         */
        KILOMETERS,

        /**
         * meters
         */
        METERS,

        /**
         * miles
         */
        MILES
    }
}
