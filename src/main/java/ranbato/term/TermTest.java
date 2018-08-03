package ranbato.term;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ranbato.term.Animation.Animation;
import ranbato.term.Animation.Entity;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class TermTest
{

    static private final Logger logger = LoggerFactory.getLogger(TermTest.class);


    // the Z depth at which certain items occur
    public enum Depth
{
    // no gui yet
    guiText(0),
    gui(1),

    // under water
    shark(2),
    fish_start(3),
    fish_end(20),
    seaweed(21),
    castle(22),

    // waterline
    water_line3(2),
    water_gap3(3),
    water_line2(4),
    water_gap2(5),
    water_line1(6),
    water_gap1(7),
    water_line0(8),
    water_gap0(9);


    Depth(int depth)
    {
        this.depth = depth;
    }

    public int getDepth()
    {
        return depth;
    }

    int depth;

}

// yes this is a total hack
    static Screen screen;

    private Random rand = new Random();


    public static void main(String[] args)
    {

        TermTest termTest = new TermTest();

        DefaultTerminalFactory def = new DefaultTerminalFactory();
        def.setForceTextTerminal(true);
        def.setInitialTerminalSize(new TerminalSize(80,80));
        try (Terminal terminal = def.createTerminal())
        {
            screen = new TerminalScreen(terminal);

            screen.setCursorPosition(null);

            screen.startScreen();

            TextGraphics graphics = screen.newTextGraphics();
            int row = 0;
            for(Map.Entry entry:Animation.COLOR_MAP.entrySet()){
                graphics.setForegroundColor((TextColor)entry.getValue());
                graphics.putString(1,row++,entry.getKey().toString());
            }
            screen.refresh();

            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < 20000) {
                // The call to pollInput() is not blocking, unlike readInput()
                if(screen.pollInput() != null) {
                    break;
                }
                try {
                    Thread.sleep(1);
                }
                catch(InterruptedException ignore) {
                    break;
                }
            }
            Entity castle = termTest.add_castle();
            Animation animation = new Animation();
            animation.add_entity(castle);
            animation.add_entity(termTest.add_environment());
            animation.add_entity(termTest.add_all_seaweed());
            animation.build_screen();

            screen.refresh();
            Thread.sleep(15000);

            screen.stopScreen();


        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }


    }

    /**
     * Stolen from Java 11
     *   Returns a string whose value is the concatenation of this
     * string repeated {@code count} times.
     * <p>
     * If this string is empty or count is zero then the empty
     * string is returned.
     *
     * @param   count number of times to repeat
     *
     * @return  A string composed of this string repeated
     *          {@code count} times or the empty string if this
     *          string is empty or count is zero
     *
     * @throws  IllegalArgumentException if the {@code count} is
     *          negative.
     */
    private static String repeat(String value ,int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count is negative: " + count);
        }
        if (count == 1) {
            return value;
        }
        final int len = value.length();
        if (len == 0 || count == 0) {
            return "";
        }
        if (len == 1) {
            return value.substring(0,1);
        }
        if (Integer.MAX_VALUE / count < len) {
            throw new OutOfMemoryError("Repeating " + len + " bytes String " + count +
                    " times will produce a String exceeding maximum size.");
        }
        final int limit = len * count;
        final byte[] multiple = new byte[limit];
        System.arraycopy(value.getBytes(), 0, multiple, 0, len);
        int copied = len;
        for (; copied < limit - copied; copied <<= 1) {
            System.arraycopy(multiple, 0, multiple, copied, copied);
        }
        System.arraycopy(multiple, 0, multiple, copied, limit - copied);
        return new String(multiple);
    }

    /**
     * Find n-th occurance of a string in a string
     * @param str string to check
     * @param searchStr String to find
     * @param n occurance to find
     * @return index or -1 if not found
     */
    private int nthIndexOf(String str, String searchStr, int n) {
        if (str == null || searchStr == null || n <= 0) {
             return -1;
                    }
        int pos = str.indexOf(searchStr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(searchStr, pos + 1);
        return pos;
    }

    private List<Entity> add_environment() {

    String [] water_line_segment = new String[]{
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
            "^^^^ ^^^  ^^^   ^^^    ^^^^      ",
            "^^^^      ^^^^     ^^^    ^^     ",
            "^^      ^^^^      ^^^    ^^^^^^  "
    };

	// tile the segments so they stretch across the screen
    int segment_size = water_line_segment[0].length();
        int segment_repeat = (screen.getTerminalSize().getColumns()/segment_size) + 1;
    for(int i = 0;i<water_line_segment.length;i++) {
        water_line_segment[i] = repeat(water_line_segment[i],segment_repeat);
    }

    List<Entity> entityList = new ArrayList<>(water_line_segment.length);
    for(int i = 0;i<water_line_segment.length;i++) {
        Entity entity = Entity.newBuilder().withName("water_seg_"+i).withType("waterline")
        .withShape(water_line_segment[i]).withPosition(0,i+5,Depth.valueOf("water_line"+i).getDepth())
                .withDefault_color(TextColor.ANSI.CYAN).withDepth(22).withPhysical(true).build();

        entityList.add(entity);

    }
    return entityList;
}

    private  Entity add_castle()
    {
        String castle_image = "\n" +
                "               T~~\n" +
                "               |\n" +
                "              /^\\\n" +
                "             /   \\\n" +
                " _   _   _  /     \\  _   _   _\n" +
                "[ ]_[ ]_[ ]/ _   _ \\[ ]_[ ]_[ ]\n" +
                "|_=__-_ =_|_[ ]_[ ]_|_=-___-__|\n" +
                " | _- =  | =_ = _    |= _=   |\n" +
                " |= -[]  |- = _ =    |_-=_[] |\n" +
                " | =_    |= - ___    | =_ =  |\n" +
                " |=  []- |-  /| |\\   |=_ =[] |\n" +
                " |- =_   | =| | | |  |- = -  |\n" +
                " |_______|__|_|_|_|__|_______|\n";

        String castle_mask = "\n" +
                "                RR\n" +
                "\n" +
                "              yyy\n" +
                "             y   y\n" +
                "            y     y\n" +
                "           y       y\n" +
                "\n" +
                "\n" +
                "\n" +
                "              yyy\n" +
                "             yy yy\n" +
                "            y y y y\n" +
                "            yyyyyyy\n";

        TerminalSize size = screen.getTerminalSize();
        Entity temp = Entity.newBuilder().withName("castle").withShape(castle_image).withColorMask(castle_mask).withPosition(size.getColumns()-32, size.getRows()-13, Depth.castle.getDepth()).withDefault_color(TextColor.ANSI.WHITE).build();

        return temp;
    }


    private List<Entity> add_all_seaweed() {
	// figure out how many seaweed to add by the width of the screen
    int seaweed_count = screen.getTerminalSize().getColumns() / 15;
    List<Entity> entityList = new ArrayList<>(seaweed_count);
    for (int i = 0;i<seaweed_count;i++) {
        entityList.add(add_seaweed());
    }

    logger.debug("'{}' seaweeds created",entityList.size());

    return entityList;
}

    private Entity add_seaweed (Object... args) {
    final String [] SEAWEED_IMAGE = {" )\n(\n )\n(\n )\n(\n )\n(\n )\n(\n )\n(\n )\n","(\n )\n(\n )\n(\n )\n(\n )\n(\n )\n(\n )\n(\n"};

    int height = rand.nextInt(4) + 3;
//        for my $i (1..$height) {
//            my $left_side = $i%2;
//            my $right_side = !$left_side;
//            $seaweed_image[$left_side] .= "(\n";
//            $seaweed_image[$right_side] .= " )\n";
//        }
    String [] seaweed_image = new String[2];
    seaweed_image[0]=SEAWEED_IMAGE[0].substring(0,nthIndexOf(SEAWEED_IMAGE[0],"\n",height));
    seaweed_image[1]=SEAWEED_IMAGE[1].substring(0,nthIndexOf(SEAWEED_IMAGE[1],"\n",height));

    int x = rand.nextInt(screen.getTerminalSize().getColumns()-2) + 1;
    int y = screen.getTerminalSize().getRows() - height;
    double anim_speed = (rand.nextDouble()*0.05) + 0.25;
    Entity entity = Entity.newBuilder().withName("seaweed" + rand.nextFloat()).withShape(seaweed_image).withPosition(x,y,Depth.seaweed.getDepth())
            .withCallback_args(0,0,0,(float)anim_speed)
            .withDie_time(Instant.now().plus(8,ChronoUnit.MINUTES).plus(Math.round(rand.nextDouble()*4),ChronoUnit.SECONDS)) // seaweed lives for 8 to 12 minutes
            .withDeath_cb(this::add_seaweed).withDefault_color(TextColor.ANSI.GREEN).build();

    return entity;

}

}

