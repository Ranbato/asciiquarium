package ranbato.term.Animation;

public class EntityBuilder
{
    private String name;
    private String shape;
    private String color;
    private int[] callback_args;

    public EntityBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    public EntityBuilder setShape(String shape)
    {
        this.shape = shape;
        return this;
    }

    public EntityBuilder setColor(String color)
    {
        this.color = color;
        return this;
    }

    public EntityBuilder setCallback_args(int[] callback_args)
    {
        this.callback_args = callback_args;
        return this;
    }

    public Entity createEntity()
    {
        return new Entity(name, shape, color,"WHITE");
    }
}