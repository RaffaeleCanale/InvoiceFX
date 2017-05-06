package app.util.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Specific code that generates a background for the App icon.
 * <p>
 * Created on 20/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class IconCreator  {

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        GradientPaint grad = new GradientPaint(100, 0, new Color(240, 240, 240), 300, 300, new Color(180, 180, 180));
        g.setPaint(grad);
        g.fillOval(20, 20, 260, 260);

        ImageIcon icon = new ImageIcon(IconCreator.class.getResource("/icons/icon_alt.png"));


//        Shape rect = new Rectangle2D.Double(0, 0, 300, 150);
//        Shape circle = new Ellipse2D.Double(0, 0, 300, 300);
//        Path2D.Double p = new Path2D.Double();
//        p.append(circle, false);
//        p.append(rect, false);

//        g.clip(p);
        g.drawImage(icon.getImage(), 5, 20, null, null);

        ImageIO.write(image, "png", new File("resources/icons/icon.png"));
    }


}
