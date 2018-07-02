package ranbato.term.Animation;

public class Point3D
{
    private int x = 0;
    private int y = 0;
    private int z = 0;

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return x;
    }

    public int getHeight()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public Point3D(int width, int height)
    {
        this.x = width;
        this.y = height;
        this.z = 0;
    }
    public Point3D(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
