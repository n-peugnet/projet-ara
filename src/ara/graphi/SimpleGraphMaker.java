
// package ara.graphi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files

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

    
    //public static class RawTable {
    
    public int[][] table = null;
    public int nbLines;
    public int nbColumns;

    public HashMap<Integer, String> columnName = new HashMap<Integer, String>();
    
    /**
     * Chargement des données et ajout à la table.
     * @param dataPath le chemin vers les données
     */
    public void loadRawTable(String dataPath) {

        ArrayList<String> lines = new ArrayList<String>();

        /** Lecture des lignes */
        try {
            File f = new File(dataPath);
            Scanner scan = new Scanner(f);
            while (scan.hasNextLine()) {
                lines.add(scan.nextLine());
            }
            scan.close();
        } catch (Exception e) { e.printStackTrace();}

        nbLines = lines.size();

        System.out.println("lineNb = " + nbLines);

        if (nbLines == 0) return;

        /** Je suppose que toutes les lignes ont la même taille (même nombre de colonnes) */
        nbColumns = lines.get(0).split(",").length;

        System.out.println("nbColumns = " + nbColumns);

        // Remplissage de la table
        // en x, les colonnes
        // en y, les lignes
        table = new int[nbColumns][nbLines];
        int y = 0;
        for (String line : lines) {

            String[] splitted = line.split(",");
            int x = 0;
            for (String val : splitted) {
                int v = Integer.parseInt(val);
                table[x][y] = v;
                x++;
            }
            y++;
        }
    }

    /**
     * Attribuer un nom à une colonne.
     * @param index  index de la colonne (donc coordonnée x)
     * @param name   nom de la colonne
     */
    public void setColumnName(int index, String name) {
        columnName.put(index, name);
    } 

    // + dans la table noms des colonnes

    class FixedColumn {
        public int value; // valeur requise
        public int index; // la position en x dans la table
        public FixedColumn(int colIndex, int requiredValue) {
            index = colIndex;
            value = requiredValue;
        }
    }
    
    class DataFilter {

        /** Colonnes fixées */
        public ArrayList<FixedColumn> fixedColumns = new ArrayList<FixedColumn>();

        public final int xColIndex;
        public final int yColIndex;
        
        /**
         * Constructeur
         * @param xColIndex l'index de la colonne à à mettre sur l'axe x
         * @param yColIndex l'index de la colonne à à mettre sur l'axe y
         */
        public DataFilter(int xColIndex, int yColIndex) {
            this.xColIndex = xColIndex;
            this.yColIndex = yColIndex;
        }

        /**
         * Fixer la valeur d'une colonne
         * @param colIndex      index de la colonne (donc x dans la table)
         * @param requiredValue valeur requise (valeur exacte, j'aurais pu faire un intervalle aussi)
         */
        public void fixColumn(int colIndex, int requiredValue) {
            fixedColumns.add(new FixedColumn(colIndex, requiredValue));
        }
    }

    public static void print(String str) {
        System.out.println(str);
    }

    /**
     * Le but est d'obtenir une liste de liste de points à dessiner.
     * Une liste de points par courbe, avec en plus la valeur maximale et minimale.
     * @return
     */
    public PlotReady prepareData(DataFilter dataFilter) {
        /**
         * 1) Filtrer les lignes qui m'intéressent
         */

        // en x on a la colonne d'index xColIndex
        // on agrège selon x
        class SomeX {
            public final int x;
            public SomeX(int x) {
                this.x = x;
            }
            public ArrayList<Integer> yValues = new ArrayList<Integer>();
        }

        ArrayList<SomeX> aggegation = new ArrayList<SomeX>();
        int xMin = -1;
        int xMax = -1;
        int yMin = -1;
        int yMax = -1;

        print("2 nbLignes = " + nbLines);

        /** Pour chaque ligne */
        for (int y = 0; y < nbLines; y++) {
            boolean passing = true;

            for (FixedColumn fixedCol : dataFilter.fixedColumns) {
                //print("val demandée ind=" + fixedCol.index + " val=" + fixedCol.value + "  current=" + table[fixedCol.index][y]);
                if (table[fixedCol.index][y] != fixedCol.value) {
                    passing = false;
                    break;
                }
            }
            //print("passing = " + passing);
            // Condition non validée
            if ( ! passing ) continue;

            /**
             * Préparer les données :
             * - Fixer les valeurs des colonnes à ne pas prendre en compte (index, value)
             * - Passer l'index de la colonne à afficher en x
             * - Passer l'index de la colonne à afficher en y
             * - Calculer le point minimal et le point maximal
             * - Retourner l'objet PlotData.
             */

            int xValue = table[dataFilter.xColIndex][y];
            int yValue = table[dataFilter.yColIndex][y];

            print("xv="+xValue + " yv="+yValue);

            SomeX foundX = null;
                for (SomeX sx : aggegation) {
                    if (sx.x == xValue) {
                    foundX = sx;
                    break;
                }
            }

            /** Nouvelle valeur pour une aggrégation en X */
            if (foundX == null) {
                foundX = new SomeX(xValue);
                aggegation.add(foundX);
            }

            /** Ajout de la valeur à l'aggrégation */
            foundX.yValues.add(yValue);
            
            if (xMin == -1) xMin = xValue;
            if (xMax == -1) xMax = xValue;
            if (yMin == -1) yMin = yValue;
            if (yMax == -1) yMax = yValue;

            if (xMin > xValue) xMin = xValue;
            if (yMin > yValue) yMin = yValue;
            if (xMax < xValue) xMax = xValue;
            if (yMax < yValue) yMax = yValue;

        }

        if (aggegation.size() == 0) return null; // TODO

        // classer l'aggrégation par exemple

        /** Nombre de lignes au total, pour ces valeurs fixées */
        int numberOfLines = aggegation.get(0).yValues.size(); // TODO : bug si pas le même nombre de lignes sur chaque agrégation

        ArrayList<PlotLine> plotLines = new ArrayList<PlotLine>();

        for (int y = 0; y < numberOfLines; y++) { // pour chaque index de ligne
            String cName = "";
            if (y == 0) {
                cName = columnName.get(dataFilter.yColIndex);
                if (cName == null) {
                    cName = "Nom introuvable";
                }
            }
            PlotLine plotLine = new PlotLine("");
            for(SomeX sx : aggegation) {
                int xPoint = sx.x;
                int yPoint = sx.yValues.get(y); // Valeur de cette ligne dans l'agrégation
                plotLine.addPoint(xPoint, yPoint);
            }
            plotLines.add(plotLine);
        }
        
        PlotReady pReady = new PlotReady();
        pReady.plotLines = plotLines;
        pReady.xMax = xMax;
        pReady.xMin = xMin;
        pReady.yMax = yMax;
        pReady.yMin = yMin;

        return pReady;
    }

    class PlotLine {
        public ArrayList<Point> points = new ArrayList<Point>();
        public final String displayName;

        public PlotLine(String displayName) {
            this.displayName = displayName;
        }

        public void addPoint(int x, int y) {
            points.add(new Point(x, y));
        }

        public int[][] formatData() {
            int[][] res = new int[points.size()][2];
            int i = 0;
            for (Point p : points) {
                res[i][0] = (int) p.getX();
                res[i][1] = (int) p.getY();
                i++;
            }
            return res;
        }
    }


    /**
     * Données prêtes à être dessinées.
     */
    class PlotReady {
        public ArrayList<PlotLine> plotLines = new ArrayList<PlotLine>();
        public int xMin = -1;
        public int xMax = -1;
        public int yMin = -1;
        public int yMax = -1;
    }


    public void makeLines(PlotReady plotReady, NamedPlotColor color, String lineTitle) {

        System.out.println("makeLines line nb = " + plotReady.plotLines.size());
        for (PlotLine plotLine : plotReady.plotLines) {
            addLine(plotLine.displayName, color, 1, plotLine.formatData());
        }
    }

    protected final List<DataSetPlot> lines = new ArrayList<DataSetPlot>();

    public static Style CURRENT_STYLE = Style.LINES;

    /**
     * Ajoute une courbe au graphique.
     * @param name   Nom à donner à la ligne
     * @param color  Couleur de la ligne (com.panayotis.gnuplot.style.NamedPlotColor)
     * @param width  Largeur de la ligne en pixels
     * @param points Points à dessiner pour former la courbe sous la forme {{x1, y1}, {x2, y2}, .. {yn, yn}}
     *               donc bien un tableau à double entrée, de taille [n, 2] avec n le nombre de points total.
     */
    public void addLine(String name, NamedPlotColor color, int width, int[][] points) {
        PlotStyle style = new PlotStyle();
        style.setStyle(CURRENT_STYLE); // com.panayotis.gnuplot.style.
        style.setLineWidth(width);
        style.setLineType(color);

        DataSetPlot linePlot = new DataSetPlot(points);
        linePlot.setPlotStyle(style);
        linePlot.setTitle(name);

        lines.add(linePlot);
    }








    public void drawTheShit(String[] args) {
        String name = args[0];
        String image = args[1];
        String datas = args[2];
        System.out.printf("%s, %s, %s\n", name, image, datas);

        // Sous la forme nodeCount,messageCount,roundCount,duration
    
        SimpleGraphMaker g = this;

        g.loadRawTable(datas);

        DataFilter dataFilter = new DataFilter(1, 2);
        dataFilter.fixColumn(0, 1); // id as round
        PlotReady plotReady = g.prepareData(dataFilter);


        DataFilter dataFilter2 = new DataFilter(1, 2);
        dataFilter2.fixColumn(0, 0); // id as round
        PlotReady plotReady2 = g.prepareData(dataFilter2);

        title = "Nombre de messages en fonction du nombre de noeuds";
        xTitle = "Nombre de noeuds";
        yTitle = "Nombre de messages";
        xMin = plotReady.xMin;
        xMax = plotReady.xMax;
        yMin = plotReady.yMin;
        yMax = plotReady.yMax;


        //SimpleGraphMaker graph = new SimpleGraphMaker("Nombre de messages en fonction du nombre de noeuds", "Nombre de noeuds", "Nombre de messages",
        //plotReady.xMin, plotReady.xMax, plotReady.yMin, plotReady.yMax);

        makeLines(plotReady, NamedPlotColor.DARK_GOLDENROD, "numéro round = id");
        makeLines(plotReady2, NamedPlotColor.DARK_VIOLET, "numéro round = 0");

        saveAs("/home/sylvain/SAR_M2/JavaPlot-0.5.0/demo/src/pd_sansss.png");
        

    }


    protected String title;
    protected String xTitle;
    protected String yTitle;
    protected int xMin;
    protected int xMax;
    protected int yMin;
    protected int yMax;
    /** Point aurait aussi marché mais + long à écrire and of course :
     *  tldr tmtc btw. */

   // protected final List<DataSetPlot> lines = new ArrayList<DataSetPlot>();

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
    /*public SimpleGraphMaker(String title, String xTitle, String yTitle,
                            int xMin, int xMax, int yMin, int yMax) {
        this.title = title;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }*/



    protected void drawOnTerminal(GNUPlotTerminal terminal) {
        try {
            JavaPlot p = new JavaPlot();
            p.setTerminal(terminal);
            p.getAxis("x").setLabel(xTitle);
            p.getAxis("y").setLabel(yTitle);
            p.getAxis("x").setBoundaries(xMin, xMax);
            p.getAxis("y").setBoundaries(yMin, yMax);
            System.out.println("lines len : " + lines.size());
            for (DataSetPlot dsp : lines) {
                p.addPlot(dsp);
            }
            p.setTitle(title);
            p.plot();
        } catch (Exception e) { }
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






    public static void main(String[] args) {
        
        SimpleGraphMaker g = new SimpleGraphMaker();
        g.drawTheShit(args);



/*
        SimpleGraphMaker graph = new SimpleGraphMaker("Nombre de messages en fonction du nombre de noeuds", "Nombre de noeuds", "Nombre de messages",
        metaData.minVal[1], metaData.maxVal[1], metaData.minVal[2], metaData.maxVal[2]);

        for (IdAsRoundContainer rc : roundContainer) { // idAsRound fixé
            
            PlotData pd = convertToPlotData(rc);

            if (rc.idAsRound == 1) {
                makeLines(rc, NamedPlotColor.DARK_GOLDENROD, "numéro round = id", graph);
            } else {
                makeLines(rc, NamedPlotColor.DARK_VIOLET, "numéro round = 0", graph);
            }
        }

        graph.saveAs("/home/sylvain/SAR_M2/JavaPlot-0.5.0/demo/src/pd_sans.png");
*/

    }



    /*static class PlotData {

        public final int lineCount; // nombre de lignes à dessiner ( = data.get(0).size())

        /** Chaque PlotDataX est à une valeur de X fixée.
         *  Dessiner une courbe, c'est fixer un index (position dans data.yValues) * /
        public ArrayList<PlotDataX> data = new ArrayList<PlotDataX>();

        public PlotData(int lineCount) {
            this.lineCount = lineCount; // = data.get(0).size()
        }
        public int xNumber() {
            return data.size(); // nombre de points par courbe
        }
    }

    // tous les y à dessiner, à x fixé
    static class PlotDataX {

        final public int x;

        /** Valeurs en y à x fixé * /
        public ArrayList<Integer> yValues = new ArrayList<Integer>();

        public PlotDataX(int x) {
            this.x = x;
        }
    }*/

}
