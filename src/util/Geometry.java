package util;

public class Geometry {
    /**
     * Get the distance from the point pt[] to the line (x1,y1)->(x2,y2)
     *
     * @param x1 x coordinate of first endpoint
     * @param y1 y coordinate of first endpoint
     * @param x2 x coordinate of second endpoint
     * @param y2 y coordinate of second endpoint
     * @param pt array of point coordinates
     * @return distance between the point and the line
     */
    public static float distanceFromPointToLine(int x1, int y1, int x2, int y2, float[] pt){
        float px = pt[0];
        float py = pt[1];

        float constant = (float) (dotProduct(x1,y1,x2,y2,px,py) / (Math.pow(x2-x1,2) + Math.pow(y2-y1,2)));

        float dx = (px-x1) - constant*(x2-x1);
        float dy = (py-y1) - constant*(y2-y1);

        return magnitude(dx,dy);
    }

    /**
     * Get the distance between two points
     *
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     * @return distance between the two points
     */
    public static float distanceFromPointToPoint(float x1, float y1, float x2, float y2){
        return (float) Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));
    }


    public static float magnitude(float x1, float y1, float x2, float y2){
        return (float) Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
    }

    /**
     * Get the magnitude of a vector
     *
     * @param x x component of the vector
     * @param y y component of the vector
     * @return vector's magnitude
     */
    public static float magnitude(float x, float y){
        return (float) Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }

    /**
     * Get the dot product of two vectors
     *
     * @param x1 x coordinate of reference point
     * @param y1 y coordinate of reference point
     * @param x2 x coordinate of first vector head
     * @param y2 y coordinate of first vector head
     * @param px x coordinate of second vector head
     * @param py y coordinate of second vector head
     * @return dot product of the vectors (x1,y1)->(x2,y2) and (x1,y1)->(px,py)
     */
    public static float dotProduct(float x1, float y1, float x2, float y2, float px, float py){
        float v1X = x2 - x1;
        float v1Y = y2 - y1;
        float v2X = px - x1;
        float v2Y = py - y1;

        return ((v1X*v2X) + (v1Y*v2Y));
    }

    /**
     * Get the dot produc of two vectors
     *
     * @param v1X x coordinate of first vector head
     * @param v1Y y coordinate of first vector head
     * @param v2X x coordinate of second vector head
     * @param v2Y y coordinate of second vector head
     * @return dot product of vectors v1 and v2
     */
    public static float dotProduct(float v1X, float v1Y, float v2X, float v2Y){
        return ((v1X*v2X) + (v1Y*v2Y));
    }

    /**
     * Get the normalized magnitude of a vector: p1 -> p2
     *
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     * @return normalized magnitude of the vector (x1,y1)->(x2,y2)
     */
    public static float[] norm(float x1, float y1, float x2, float y2){
        float mag = magnitude(x1,y1,x2,y2);

        float xComp = x2-x1;
        float yComp = y2-y1;

        float[] normal = new float[2];
        normal[0] = xComp / mag;
        normal[1] = yComp / mag;

        return normal;
    }

    public static float[] norm3D(float[] vector){
        float squaredSum = vector[0]*vector[0] + vector[1]*vector[1] + vector[2]*vector[2];
        float vectorizer = invSqrt(squaredSum);
        float[] normalized = new float[]{
                vector[0]*vectorizer,
                vector[1]*vectorizer,
                vector[2]*vectorizer
        };
        return normalized;
    }

    public static float[] normal(float[] aPoint, float[] bPoint, float[] nPoint){
        float[] vectorA = new float[]{
                aPoint[0]-nPoint[0],
                aPoint[1]-nPoint[1],
                aPoint[2]-nPoint[2]
        };
        float[] vectorB = new float[]{
                bPoint[0]-nPoint[0],
                bPoint[1]-nPoint[1],
                bPoint[2]-nPoint[2]
        };
        return norm3D(crossProduct(vectorA, vectorB));
    }

    public static float[] crossProduct(float[] vectorA, float[] vectorB){
        return new float[]{
                vectorA[1]*vectorB[2]-vectorA[2]*vectorB[1],
                vectorA[2]*vectorB[0]-vectorA[0]*vectorB[2],
                vectorA[0]*vectorB[1]-vectorA[1]*vectorB[0]
        };
    }

    /**
     * Accurate approximation for the inverse (reciprocal) of a floating-point square root.
     *
     * @param x
     * @return float square root
     */
    public static float invSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x *= (1.5f - xhalf * x * x);
        return x;
    }
}
