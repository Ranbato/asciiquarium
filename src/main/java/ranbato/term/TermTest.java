package ranbato.term;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import ranbato.term.Animation.Animation;
import ranbato.term.Animation.Entity;
import ranbato.term.Animation.EntityBuilder;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class TermTest
{


    public static void main(String[] args)
    {

        DefaultTerminalFactory def = new DefaultTerminalFactory();
        def.setForceTextTerminal(true);
        try (Terminal terminal = def.createTerminal())
        {
            Screen screen = new TerminalScreen(terminal);

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
            Entity castle = add_castle();
            Animation animation = new Animation();
            animation.add_entity(castle);
            animation.build_screen();

            screen.refresh();
            Thread.sleep(15000);


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
    public static String repeat(String value ,int count) {
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

    private static Entity add_environment() {

    String [] water_line_segment = new String[]{
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
            "^^^^ ^^^  ^^^   ^^^    ^^^^      ",
            "^^^^      ^^^^     ^^^    ^^     ",
            "^^      ^^^^      ^^^    ^^^^^^  "
    };

	// tile the segments so they stretch across the screen
    int segment_size = water_line_segment[0].length();
    //int segment_repeat = int($anim->width()/segment_size) + 1;
        int segment_repeat = (40/segment_size) + 1;
    for(int i = 0;i<water_line_segment.length;i++) {
        water_line_segment[i] = repeat(water_line_segment[i],segment_repeat);
    }

    for(int i = 0;i<water_line_segment.length;i++) {
        $anim->new_entity(
                name		=> "water_seg_"+i,
                type		=> "waterline",
                shape		=> $water_line_segment[i],
                position	=> [ 0, i+5, $depth{'water_line'  . i} ],
        default_color	=> 'cyan',
                depth		=> 22,
                physical	=> 1,
		);
    }
}

    private static Entity add_castle()
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

        Entity temp = new EntityBuilder().setName("castle").setShape(castle_image).setColor(castle_mask).createEntity();//.setDefaultColor(TextColor.ANSI.BLACK);
        temp.setDefault_color("BLACK");
        temp.setPosition(10, 2, 1);

//    $anim->new_entity(
//            name		=> "castle",
//            shape		=> $castle_image,
//            color		=> $castle_mask,
//            position	=> [ $anim->width()-32, $anim->height()-13, $depth{'castle'} ],
//    default_color	=> 'BLACK',
//	);
        return temp;
    }
}

