package ranbato.term.Animation;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;

import java.lang.invoke.MethodHandle;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.*;

/*
=head1 NAME

Term::Animation::Entity

=head1 SYNOPSIS

  use Term::Animation::Entity;

  // Constructor
  my $entity = Term::Animation::Entity->new(
      shape         => ';-)',
      position      => [ 1, 2, 3 ],
      callback_args => [ 0, 1, 0, 0 ],
  );

=head1 ABSTRACT

A sprite object for use with Term::Animation

=head1 DESCRIPTION

Term::Animation::Entity is used by L<Term::Animation|Term::Animation> to
represent a single sprite on the screen.

=head1 PARAMETERS

  name < SCALAR >
        A string uniquely identifying this object

  shape < REF >
        The ASCII art for this object. It can be provided as:
                  1) A single multi-line text string (no animation)
                  2) An array of multi-line text strings, where each
		     element is a single animation frame
                  3) An array of 2D arrays. Each element in the outer
		     array is a single animation frame.
        If you provide an array, each element is a single frame of animation.
	If you provide either 1) or 2), a single newline will be stripped off
	of the beginning of each string. 3) is what the module uses internally.

  auto_trans < BOOLEAN >
        Whether to automatically make whitespace at the beginning of each line
	transparent.  Default: 0

  position < ARRAY_REF >
        A list specifying initial x,y and z coordinates
        Default: [ 0, 0, 0 ]

  callback < SUBROUTINE_REF >
        Callback routine for this entity. Default: I<move_entity()>

  callback_args < REF >
        Arguments to the callback routine.

  curr_frame < INTEGER >
        Animation frame to begin with. Default: 0

  wrap < BOOLEAN >
        Whether this entity should wrap around the edge of the screen. Default: 0

  transparent < SCALAR >
        Character used to indicate transparency. Default: ?

  die_offscreen < BOOLEAN >
  	Whether this entity should be killed if
	it goes off the screen. Default: 0

  die_entity < ENTITY >
  	Specifies an entity (ref or name). When the named
	entity dies, this entity should die as well. Default: undef

  die_time < INTEGER >
  	The time at which this entity should be killed. This 
	should be a UNIX epoch time, as returned
	by I<time>.  Default: undef

  die_frame < INTEGER >
  	Specifies the number of frames that should be displayed
	before this entity is killed. Default: undef

  death_cb < SUBROUTINE_REF >
        Callback routine used when this entity dies

  dcb_args < REF >
        Arguments to the entity death callback routine

  color
        Color mask. This follows the same format as 'shape'.
	See the 'COLOR' section below for more details.

  default_color < SCALAR >
        A default color to use for the entity.  See the 'COLOR' section
	for more details.

  data < REF >
  	Store some data about this entity. It is not used by the module.
	You can use it to store state information about this entity.

=head1 METHODS

=over 4

=item I<new>

  my $entity = Term::Animation::Entity->new(
      shape         => ';-)',
      position      => [ 1, 2, 3 ],
      callback_args => [ 0, 1, 0, 0 ],
  );

Create a Term::Animation::Entity instance. See the PARAMETERS section for
details.

=cut
*/
public class Entity
{

    static final private Logger logger = LoggerFactory.getLogger(Entity.class);

    private String name;
    //    private Animation animation;
    // default to single asterisk
    private char[][][] shape = new char[][][]{{{'*'}}};
    private char[][][] colorMask;
    private String originalMask;
    private TextCharacter[][][] drawCache;
    private int width, height;
    // appearance
    private char transparent = '?';
    private boolean auto_trans = false;
    private int x, y, z;
    private TextColor default_color = TextColor.ANSI.WHITE;
    private TextColor background_color = TextColor.ANSI.BLACK;

    // collision detection
    private int depth;
    private boolean physical;
    private AnimationPath callback_args;

    // behavior
    private boolean wrap = false;
    private Entity follow_entity;
    private int follow_offset;

    // state
    private int curr_frame = 0;


    private class AnimationPath {
        private int frame = -1;
        private float [][] path;

        public int getFrame()
        {
            return frame;
        }
        public void nextFrame()
        {
            this.frame++;
            if(frame >= path.length){
                frame = 0;
            }
        }
        public void setFrame(int frame)
        {
            this.frame = frame;
        }

        public float[] getCurrentPath()
        {
            return frame == -1?path[0]:path[frame];
        }

        public void setPath(float[][] path)
        {
            this.path = path;
        }
    }


    public Entity(String name, String shape, String colorMask, String default_color)
    {
        this.name = name;
        this.shape = build_shape(shape);
        this.colorMask = build_mask(colorMask);
    }

    /**
     * Returns a reference to a list of entities that this entity
     * collided with during this animation cycle.
     *
     * @return
     */
    public List<Entity> getCollisions()
    {
        return collisions;
    }

    public void setCollisions(List<Entity> collisions)
    {
        this.collisions = collisions;
    }

    private List<Entity> collisions = new LinkedList<>();

    // entity death
    private boolean die_offscreen = false;
    private Instant die_time;
    private int die_frame;
    private MethodHandle death_cb;
    private Entity die_entity;

    //misc
    private String type;
    private String data;

    public TextCharacter[][] getCurrentFrame()
    {
        return drawCache[curr_frame];
    }

    public Entity entityBuilder()
    {
        return null;

//	my $proto = shift;
//	my $class = ref($proto) || $proto;
//	my $self = {};
//	my %p = @_;
//
//	// default sprite is a single asterisk
//	unless(defined($p{'shape'})) { $p{'shape'} = '*'; }
//
//	// if no name is supplied, generate a random one
//	if(defined($p{'name'})) {
//		$self->{NAME} = $p{'name'};
//	} else {
//		my $rand_name = rand();
//		while(defined($self->{OBJECTS}{$rand_name})) {
//			$rand_name = rand();
//		}
//		$self->{NAME} = $rand_name;
//	}
//
//	// appearance
//	$self->{TRANSPARENT}		= defined($p{'transparent'})	? $p{'transparent'}		: '?';
//	$self->{AUTO_TRANS}		= defined($p{'auto_trans'})	? $p{'auto_trans'}		: 0;
//	if($self->{AUTO_TRANS}) { $p{'shape'} = _auto_trans($p{'shape'}, $self->{TRANSPARENT}); }
//	($self->{SHAPE}, $self->{HEIGHT}, $self->{WIDTH}) = _build_shape($self, $p{'shape'});
//	($self->{X}, $self->{Y}, $self->{Z})	= defined($p{'position'})	? @{$p{'position'}}		: ( 0, 0, 0 );
//	$self->{DEF_COLOR}		= defined($p{'default_color'})	? Term::Animation::color_id($p{'default_color'}) : 'w';
//	_build_mask($self, $p{'color'});
//
//	// collision detection
//	$self->{DEPTH}		= defined($p{'depth'})        ? $p{'depth'}        : 1;
//	$self->{PHYSICAL}	= defined($p{'physical'})     ? $p{'physical'}     : 0;
//	$self->{COLL_HANDLER}	= defined($p{'coll_handler'}) ? $p{'coll_handler'} : undef;
//
//	// behavior
//	$self->{CALLBACK_ARGS}	= defined($p{'callback_args'})? $p{'callback_args'}: undef;
//	$self->{WRAP}		= defined($p{'wrap'})         ? $p{'wrap'}         : 0;
//	if   (defined($p{'callback'}))      { $self->{CALLBACK} = $p{'callback'}; }
//	elsif(defined($p{'callback_args'})) { $self->{CALLBACK} = \&move_entity;  }
//	else                                { $self->{CALLBACK} = undef;          }
//	$self->{FOLLOW_ENTITY}  = defined($p{'follow_entity'})? $self->_get_entity_name($p{'follow_entity'}) : undef;
//	$self->{FOLLOW_OFFSET}  = defined($p{'follow_offset'})? $p{'follow_offset'} : undef;
//
//	// state
//	$self->{CURR_FRAME}	= defined($p{'curr_frame'})   ? $p{'curr_frame'}   : 0;
//
//	// entity death
//	$self->{DIE_OFFSCREEN}  = defined($p{'die_offscreen'}) ? $p{'die_offscreen'} : 0;
//	$self->{DIE_TIME}       = defined($p{'die_time'})      ? $p{'die_time'}      : undef;
//	$self->{DIE_FRAME}      = defined($p{'die_frame'})     ? $p{'die_frame'}     : undef;
//	$self->{DEATH_CB}	= defined($p{'death_cb'})      ? $p{'death_cb'}      : undef;
//	$self->{DCB_ARGS}	= defined($p{'dcb_args'})      ? $p{'dcb_args'}      : undef;
//	$self->{DIE_ENTITY}     = defined($p{'die_entity'})    ? $self->_get_entity_name($p{'die_entity'}) : undef;
//
//	// misc
//	$self->{TYPE}		= defined($p{'type'})		? $p{'type'}	: "self";
//	$self->{DATA}		= defined($p{'data'})		? $p{'data'}	: undef;
//
//	bless($self, $class);
//	return $self;
    }


    /**
     * Enables or disabled collision detection for this entity.
     */
    public void setPhysical(boolean new_physical)
    {
        if (new_physical != physical)
        {
            physical = new_physical;
//			if(animation != null)) {
//				animation = _update_physical(this);
        }
    }


    public boolean getPhysical()
    {
        return physical;
    }

    /**
     * Enables or disables automatic transparency for this entity's sprite.
     * This will only affect subsequent calls to I<shape>, the current sprite
     * will be unchanged.
     *
     * @param value
     */
    public void setauto_trans(boolean value)
    {
        auto_trans = value;
    }

    public boolean getauto_trans()
    {
        return auto_trans;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public char[][][] getShape()
    {
        return shape;
    }

    public void setShape(char[][][] shape)
    {
        this.shape = shape;
    }

    public char[][][] getColorMask()
    {
        return colorMask;
    }

    public void setColorMask(char[][][] colorMask)
    {
        this.colorMask = colorMask;
    }

    public char getTransparent()
    {
        return transparent;
    }

    /**
     * Gets or sets the transparent character for this entity's sprite.
     * This will only affect subsequent calls to I<shape>, the current
     * sprite will be unchanged.
     *
     * @param transparent
     */
    public void setTransparent(char transparent)
    {
        this.transparent = transparent;
    }

    public boolean isAuto_trans()
    {
        return auto_trans;
    }


    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public String getDefault_color()
    {
        return default_color.toString();
    }

    public void setDefault_color(String default_color)
    {
        this.default_color = TextColor.ANSI.valueOf(default_color);
        build_mask();
    }

    public int getDepth()
    {
        return depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public boolean isPhysical()
    {
        return physical;
    }


    public boolean isWrap()
    {
        return wrap;
    }

    /**
     * Gets or sets the boolean that indicates whether this entity
     * should wrap around when it gets to an edge of the screen.
     *
     * @param wrap
     */
    public void setWrap(boolean wrap)
    {
        this.wrap = wrap;
    }

    public Entity getFollow_entity()
    {
        return follow_entity;
    }

    public void setFollow_entity(Entity follow_entity)
    {
        this.follow_entity = follow_entity;
    }

    public int getFollow_offset()
    {
        return follow_offset;
    }

    public void setFollow_offset(int follow_offset)
    {
        this.follow_offset = follow_offset;
    }

    public int getCurr_frame()
    {
        return curr_frame;
    }

    public void setCurr_frame(int curr_frame)
    {

        if (curr_frame < 0 || curr_frame > shape.length - 1)
        {
            logger.error("Invalid frame number: {}", curr_frame);
            return;
        }
        this.curr_frame = curr_frame;
    }

    public boolean isDie_offscreen()
    {
        return die_offscreen;
    }

    /**
     * Get or set the flag that indicates whether this
     * entity should die when it is entirely off the screen.
     *
     * @param die_offscreen
     */
    public void setDie_offscreen(boolean die_offscreen)
    {
        this.die_offscreen = die_offscreen;
    }

    public Instant getDie_time()
    {
        return die_time;
    }

    /**
     * @param die_time
     */
    public void setDie_time(Instant die_time)
    {
        this.die_time = die_time;
    }

    public int getDie_frame()
    {
        return die_frame;
    }

    /**
     * Get or set the frame number in which this entity
     * should die, counting from the time when die_frame
     * is called. Set to -1 to disable.
     *
     * @param die_frame
     */
    public void setDie_frame(int die_frame)
    {
        this.die_frame = die_frame;
    }

    public MethodHandle getDeath_cb()
    {
        return death_cb;
    }

    public void setDeath_cb(MethodHandle death_cb)
    {
        this.death_cb = death_cb;
    }

    public Entity getDie_entity()
    {
        return die_entity;
    }

    /**
     * Get or set an entity whose death will cause the
     * death of this entity. Set to null to disable.
     *
     * @param die_entity
     */
    public void setDie_entity(Entity die_entity)
    {
        this.die_entity = die_entity;
    }

    public String getType()
    {
        return type;
    }

    /**
     * Get or set the 'type' of the entity. The type can be any string,
     * and is not used by the animation itself.
     *
     * @param type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    public String getData()
    {
        return data;
    }

    /**
     * Get or set the 'data' associated with the entity. It should
     * be a single scalar or ref. This can be whatever you want,
     * it is not used by the module and is provided for convenience.
     *
     * @param data
     */
    public void setData(String data)
    {
        this.data = data;
    }

    /**
     * Returns the X / Y / Z dimensions of the entity.
     *
     * @return
     */
    public Point3D getSize()
    {
        return new Point3D(width, height, depth);
    }

    /**
     * Gets or sets the X / Y / Z coordinates of the entity.
     * <p>
     * Note that you should normally position an entity using its callback routine,
     * instead of calling one of these methods.
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets or sets the X / Y / Z coordinates of the entity.
     * <p>
     * Note that you should normally position an entity using its callback routine,
     * instead of calling one of these methods.
     */
    public void setPosition(Point3D point)
    {
        this.x = point.getX();
        this.y = point.getY();
        this.z = point.getZ();
    }

    public Point3D getPosition()
    {
        return new Point3D(x, y, z);
    }



//
//=item I<die_time>
//
//  $entity->die_time( time() + 20 );
//  $die_time = $entity->die_time;
//


//
//=cut
//sub shape {
//	my $self = shift;
//	if(@_) {
//		my $shape = shift;
//		if($self->{AUTO_TRANS}) {
//			$shape = _auto_trans($shape, $self->{TRANSPARENT});
//		}
//		($self->{SHAPE},$self->{HEIGHT},$self->{WIDTH}) = $self->_build_shape($shape);
//	}
//}
//
//
//=item I<animation>
//
//  $entity->animation( $anim );
//  $anim = $entity->animation();
//
//Get or set the Term::Animation object that this entity is
//part of.
//
//=cut
//sub animation {
//	my $self = shift;
//	if(@_) { $self->{ANIMATION} = shift; }
//	return $self->{ANIMATION};
//}
//


    /**
     * The default callback. You can also override and/or call this from your own
     * callback to do the work of moving and animating the entity
     * after you have done whatever other processing you want to do.
      */
public int []  move_entity(Animation anim) {
	float [] cb_args;
	float f = 0;
	// figure out if we just have a set of deltas, or if we have
	// a full animation path to follow
	if(callback_args.getFrame() != -1) {
		cb_args = callback_args.getCurrentPath();
        callback_args.nextFrame();
		f = cb_args[3];
	} else {
        cb_args = callback_args.getCurrentPath();
		if(cb_args.length == 4) {
			f = curr_frame + cb_args[3];
			f = (float) ((f - Math.floor(f)) + (f % (shape.length) + 1));
		}
	}

	float x = (getX() + cb_args[0]);
	float y = (getY() + cb_args[1]);
	float z = (getZ() + cb_args[2]);

	// @todo should this all be handled in the Animation class?
	if(isWrap()) {
		if(x >= anim.size.getColumns())  { x = (float) ((x - Math.floor(x)) + (x % anim.size.getColumns()));  }
		else if(x < 0)            { x = (float)(x - Math.floor(x)) + (x % anim.size.getColumns());  }
		if(y >= anim.size.getRows()) { y = (float)(y - Math.floor(y)) + (y % anim.size.getRows()); }
		else if(y < 0)            { y = (float)(y - Math.floor(y)) + (y % anim.size.getRows()); }
	}
	return new int [] {Math.round(x), Math.round(y), Math.round(z), Math.round(f)};
}

    /**
     * Rebuild mask if something else changes, e.g. default_color
     * @return
     */
    private char[][][] build_mask(){
        return build_mask(null);
    }

    /**
     * create a color mask for an entity and update the drawcache
     * @param shape mask shape string
     * @return mask color array
     */
    private char[][][] build_mask(String shape)
    {


        // store the color mask in case we are asked to
        // change the default color later

        char[][][] mask = new char[1][][];
        if (shape != null)
        {
            originalMask = shape;
            mask = build_shape(shape, false);
        }
        else if (originalMask != null)
        {
            shape = originalMask;
            mask = build_shape(shape, false);
        }
        else
        {
            logger.error("mask and original are null ");
        }

        // make sure mask is same size as shape
        int maskWidth = 0;
        int maskHeight = 0;
        for (int i = 0; i < mask.length; i++)
        {
            Point3D size = getFrameSize(mask[i]);
            if (maskWidth <= size.getWidth())
            {
                maskWidth = size.getWidth();
            }
            if (maskHeight <= size.getHeight())
            {
                maskHeight = size.getHeight();
            }
        }
        if (maskWidth > width || maskHeight > height)
        {
            logger.warn("Mask '{}' [{},{}] is larger than shape '{}'[{},{}]", shape, maskWidth, maskHeight, this.shape, width, height);
        }
        else if (maskWidth < width || maskHeight < height)
        {
            // copy frames into larger
            char[][][] newmask = new char[mask.length][height][];
            for (int f = 0; f < mask.length; f++)
            {
                for (int i = 0; i < maskHeight; i++)
                {
                    newmask[f][i] = Arrays.copyOf(mask[f][i], width);
                }
            }
            mask = newmask;
        }

        // if we were given fewer mask frames
        // than we have animation frames, then
        // repeat what we got to make up the difference.
        // this allows the user to pass a single color
        // mask that is the same for every animation frame
        if (mask.length < getShape().length)
        {
            int diff = getShape().length - mask.length;
            char[][][] newmask = Arrays.copyOf(mask, getShape().length);
            for (int i = mask.length; i < getShape().length; i++)
            {
                newmask[i] = mask[i - 1];
            }
            mask = newmask;
        }

        // update drawcache
        drawCache = new TextCharacter[getShape().length][height][width];
        for (int f = 0; f < drawCache.length; f++)
        {
            for (int i = 0; i < height; i++)
            {
                for (int j = 0; j < width; j++)
                {

                    if(j>= this.shape[f][i].length){
                        // make this transparent?
                        drawCache[f][i][j] = null;
                        continue;
                    }
                    TextColor color = default_color;
                    if (mask[f][i][j] != ' ' && mask[f][i][j] != '\u0000')
                    {
                        // make sure it's a valid color
                        color = Animation.COLOR_MAP.get(Character.toString(mask[f][i][j]).toUpperCase());
                        if (color == null)
                        {
                            logger.error("Invalid color mask: [{}][{}][{}]:'{}'", f, i, j, mask[f][i][j]);
                            color = default_color;
                        }
                    }

                        // capital letters indicate bold colors
                        if (Character.isUpperCase(mask[f][i][j]))
                        {
                            drawCache[f][i][j] = new TextCharacter(this.shape[f][i][j], color, background_color, SGR.BOLD);
                        }
                        else
                        {

                            drawCache[f][i][j] = new TextCharacter(this.shape[f][i][j], color, background_color);
                        }

                    }
                }
            }




        return mask;
    }


    /**
     * automatically make whitespace appearing on a line before the first non-
     * whitespace character transparent
     */
    private void auto_trans()
    {

        for (char[][] frame : shape)
            for (char[] line : frame)
            {
                int index = 0;
                while (Character.isWhitespace(line[index]))
                {
                    line[index] = transparent;
                }
            }
    }

    private char[][][] build_shape(String myShape)
    {
        return build_shape(myShape, true);
    }

    /**
     * take one of 1) a string 2) an array of strings 3) an array of 2D arrays
     * use these to generate a shape in the format we want (which is #3 above)
     */
    private char[][][] build_shape(String myShape, boolean setSize)
    {
        String[] lines = myShape.split("\n");
        char[][][] result = new char[1][][];
        result[0] = build_frame(lines);
        Point3D size = getFrameSize(result[0]);
        if (setSize)
        {
            width = size.getWidth();
            height = size.getHeight();
        }
        return result;
    }

    private char[][][] build_shape(String[] myShape)
    {
        return build_shape(myShape, true);
    }

    private char[][][] build_shape(String[] myShape, boolean setSize)
    {
        char[][][] result = new char[myShape.length][][];
        if (setSize)
        {
            width = 0;
            height = 0;
        }

        for (int i = 0; i < myShape.length; i++)
        {
            String frame = myShape[i];
            String[] lines = frame.split("\n");

            result[i] = build_frame(lines);

            if (setSize)
            {
                // update sizes
                Point3D size = getFrameSize(result[i]);
                if (width <= size.getWidth())
                {
                    width = size.getWidth();
                }
                if (height <= size.getHeight())
                {
                    height = size.getHeight();
                }
            }

        }
        return result;
    }

    private char[][] build_frame(String[] lines)
    {
        int size = lines.length;
        if (size == 0)
        {
            // @todo log this, shouldn't happen
            return new char[][]{{' '}};
        }
        int index = 0;

        // strip an empty line from the top, for convenience???  I think this was for aesthetic purposes in the perl code.
        if (lines[0] == null || lines[0].isEmpty())
        {
            index++;
            size--;
        }
        char[][] data = new char[size][];
        for (int i = 0; i < size; i++, index++)
        {
            data[i] = lines[index].toCharArray();
        }

        return data;
    }

    private Point3D getFrameSize(char[][] frame)
    {
        int h = 0;
        int w = 0;

        Point3D size;
        h = frame.length;
        for (int i = 0; i < h; i++)
        {
            if (w < frame[i].length)
            {
                w = frame[i].length;
            }
        }
        return new Point3D(w, h);
    }


}

