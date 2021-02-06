package ara.graphi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;
import com.panayotis.gnuplot.terminal.SVGTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal; // ExpandableTerminal

class SimpleGraphMaker {

    public static void main(String[] args) {
        String name = args[0];
        String image = args[1];
        String datas = args[2];
        System.out.printf("%s, %s, %s\n", name, image, datas);
    }

    /** Exemple simple d'utilisation */
    public static void simpleUsageExample() {
        int size = 20;

        SimpleGraphMaker graph = new SimpleGraphMaker("Ce qui se passe dans notre espace...", "Le temps qui passe", "Les choses mesurées",
        0, size, 0, size);


        double[][] v = new double[size][2];
        double[][] vp = new double[size][2];

        Random r = new Random();

        for (int i = 0; i < size; i++) {
            v[i][0] = i; // x
            v[i][1] = i * 0.1 + r.nextInt(size) * 0.8; // y
            vp[i][0] = i;
            vp[i][1] = 2 + i / 3 + r.nextInt(size) * 0.05;
        }

        graph.addLine("Le mood de Nicolas", NamedPlotColor.DARK_GOLDENROD, 2, v);
        graph.addLine("Avancement du projet", NamedPlotColor.DARK_VIOLET, 2, vp);
        
        graph.saveAs("/home/sylvain/SAR_M2/JavaPlot-0.5.0/demo/src/youhou.png");
    }








    protected final String title;
    protected final String xTitle;
    protected final String yTitle;
    protected final int xMin;
    protected final int xMax;
    protected final int yMin;
    protected final int yMax;
    /** Point aurait aussi marché mais + long à écrire and of course :
     *  tldr tmtc btw. */

    protected final List<DataSetPlot> lines = new ArrayList<DataSetPlot>();

    /**
     * Outil de création de graphe, simplifié.
     * @param title  Titre du graphe.
     * @param xTitle Légende de l'axe des abscisses
     * @param yTitle Légende de l'axe désordonné
     * @param xMin   Origine en x
     * @param xMax   Origine en y
     * @param yMin   Coordonnée max représentée en x
     * @param yMax   Coordonnée max représentée en y
     */
    public SimpleGraphMaker(String title, String xTitle, String yTitle,
                            int xMin, int xMax, int yMin, int yMax) {
        this.title = title;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    /**
     * Ajoute une courbe au graphique.
     * @param name   Nom à donner à la ligne
     * @param color  Couleur de la ligne (com.panayotis.gnuplot.style.NamedPlotColor)
     * @param width  Largeur de la ligne en pixels
     * @param points Points à dessiner pour former la courbe sous la forme {{x1, y1}, {x2, y2}, .. {yn, yn}}
     *               donc bien un tableau à double entrée, de taille [n, 2] avec n le nombre de points total.
     */
    public void addLine(String name, NamedPlotColor color, int width, double[][] points) {
        PlotStyle style = new PlotStyle();
        style.setStyle(Style.LINES);
        style.setLineWidth(width);
        style.setLineType(color);

        DataSetPlot linePlot = new DataSetPlot(points);
        linePlot.setPlotStyle(style);
        linePlot.setTitle(name);

        lines.add(linePlot);
    }

    protected void drawOnTerminal(GNUPlotTerminal terminal) {
        JavaPlot p = new JavaPlot();
        p.setTerminal(terminal);
        p.getAxis("x").setLabel(xTitle);
        p.getAxis("y").setLabel(yTitle);
        p.getAxis("x").setBoundaries(xMin, xMax);
        p.getAxis("y").setBoundaries(yMin, yMax);
        for (DataSetPlot dsp : lines) {
            p.addPlot(dsp);
        }
        p.setTitle(title);
        p.plot();
    }

    public void saveAs(String filePath) { // String fileFormat, 
        String extension = ""; // par défaut, une image

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i+1).toLowerCase();
        }

        if (extension.equals("")) return;

        if (extension.equals("png")) {

            // Création du terminal
            ImageTerminal png = new ImageTerminal();

            // Création d'un nouveau fichier
            File file = new File(filePath);
            try {
                try { file.delete(); } catch (Exception e) {}
                file.createNewFile();
                png.processOutput(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
                System.err.print(ex);
            } catch (IOException ex) {
                System.err.print(ex);
            }

            // Dessin sur le terminal
            drawOnTerminal(png);

            // Fermeture du fichier
            try {
                ImageIO.write(png.getImage(), "png", file);
            } catch (IOException ex) {
                System.err.print(ex);
            }

            return;
        }


        if (extension.equals("svg")) {
            SVGTerminal svg = new SVGTerminal(filePath);
            // Dessin sur le terminal
            drawOnTerminal(svg);
            return;
        }
        
        System.err.print("SimpleGraphMaker.saveAs("+filePath+") : wrong extension, only allowing png and svg.");
    }

}
