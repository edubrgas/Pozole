package pozole;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author 
 */
public class Tablero extends JFrame
{
    private final JButton[][] jBoard = new JButton[4][4];   
    private final LinkedHashMap puzzle = new LinkedHashMap();
    private BufferedImage empty;
    private boolean dept = false;
    Set<State> estadosVisitados = new HashSet<>();
    private int dr = 0;
    private File imgFile = new File("D:\\Tareas poli\\ESCOM\\4\\FIA\\1\\2nda practica\\Pozole\\imagenes\\gato.jpg");
    
    private final String start = "1234567890:;<=>?";
    private final String goal  = "1234567809:;<=>?"; 
   
    private final JMenuItem solveB = new JMenuItem("Solve BFS");
    private final JMenuItem solveD = new JMenuItem("Solve DFS");
    
    private final Scanner keyb = new Scanner(System.in);
    private final int maxDept = 100000; // Para limitar la profundidad del árbol
    
    public Tablero()  // Constructor
    {       
        initComponents();
    }
      
    private void initComponents() 
    {
        this.setTitle("16-Puzzle");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        int width = pantalla.width;
        int height = pantalla.height;
        this.setBounds((width-128)/2,(height-924)/2,900,924);
        
        JMenuBar mainMenu = new JMenuBar();
        JMenu    file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        
        mainMenu.add(file);
        file.add(solveB);
        file.add(solveD);
        file.add(exit);
        this.setJMenuBar(mainMenu);
        
        this.setLayout(null);
        this.imagePieces(imgFile);
        paintPieces();
        exit.addActionListener(evt -> gestionarExit(evt));  
        solveB.addActionListener(evt -> whichMethod(evt)); 
        solveD.addActionListener(evt -> whichMethod(evt));  
               
        // Handle the X-Button 
        class MyWindowAdapter extends WindowAdapter
        {
            @Override
            public void windowClosing(WindowEvent eventObject)
            {
		goodBye();
            }
        }
        addWindowListener(new MyWindowAdapter());       
    }
    
    private void goodBye()
    {
        int respuesta = JOptionPane.showConfirmDialog(rootPane, "Are you sure?","Exit",JOptionPane.YES_NO_OPTION);
        if(respuesta==JOptionPane.YES_OPTION) System.exit(0);
    }
        
    private void gestionarExit(ActionEvent e)
    {
        goodBye();
    }
      
   
   // Parte la imagen en piezas 
    private void imagePieces(File pathName)
    {
        
        //Bloque "try" por que hay una tarea de lectura de archivo
        try  
        {      
            BufferedImage buffer= ImageIO.read(pathName);
            BufferedImage subImage;
            int n=0;
            for(int i=0;i<4;i++)
                for(int j=0;j<4;j++)
                {
                    subImage = buffer.getSubimage((j)*124, (i)*119, 124, 119);  // Extrae un fragmento de la imagen 
                    String k = goal.substring(n,n+1);
                    puzzle.put(k, subImage); // Almacena las piezas etiquetándolas con base en el estado final
                    n++;
                }
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.out);
        }
              
    } 
    
    public void paintPieces()
    {
        int n=0;
        for(int i=0;i<4;i++)
            for(int j=0;j<4;j++)
            {
                String  k=start.substring(n,n+1);
                BufferedImage subImage = (BufferedImage) puzzle.get(k);   
                jBoard[i][j] = new JButton();
                jBoard[i][j].setBounds(j*166+1, i*166+1,166,166); // Calcula la posición del botón i,j
                this.add(jBoard[i][j]);                                                                        
                if(!k.equals("0"))jBoard[i][j].setIcon(new ImageIcon(subImage));
                else empty = subImage;
                n++;                 
            }

    }
    
    // Este es el método que realmente busca mediante le técnica en anchura
    
    private void whichMethod(ActionEvent e)
    {
        if(e.getSource()==solveD) dept=true;  // En caso de que se trate de búsqueda en profundidad
        solve();
    }
    
    
    private void solve()
    {
        boolean success = false;
        int deadEnds = 0;
        int totalNodes = 0;
        State startState = new State(start);
        State goalState   = new State(goal);
        ArrayDeque queue = new ArrayDeque();
        ArrayList<State> first = new ArrayList();
        //State first = new State(start);
        ArrayList<State> path=null;
        solveB.setEnabled(false);
        solveD.setEnabled(false);
        
        first.add(startState);
        queue.add(first);
        

        // Ciclo de búsqueda
        
        boolean deepCond = false;
        dr=0;
        //Loops
        int m=0;
        long startTime = System.currentTimeMillis();
        while(!queue.isEmpty() && !success && !deepCond)
        {
            m++;
            int validStates = 0;      
            //System.out.println("Ciclo " + m);
            ArrayList<State> l = (ArrayList<State>) queue.getFirst();
            //System.out.println("Analizando Ruta de :" + l.size());
            //muestraEstados(l);
            State last = (State) l.get(l.size()-1);
            //last.show();
            ArrayList<State> next = last.nextStates();  
            //System.out.println("Se encontraron " + next.size()+ " estados sucesores posibles");
            totalNodes+=next.size();
            
            queue.removeFirst(); // Se elimina el primer camino de la estrutura
 
            for(State ns: next)
            {
                if(!repetido(l, ns)) // Se escribió un método propio para verificar repetidos
                {
                    validStates++;
                    ArrayList<State> nl = (ArrayList<State>) l.clone();
                    if(ns.goalFunction(goalState))
                    {
                        success = true;
                        path = nl;
                    }
                    // for(State s: nl)
                        if(!buscar(ns)) 
                        nl.add(ns);     
                    
                    if(nl.size()-1>dr) dr=nl.size()-1;
                   
                    if(dr > maxDept) deepCond = true;
                    
                    //muestraEstados(nl);
                    if(dept)
                        queue.addFirst(nl); // Si es en profundidad agrega al principio la nueva ruta
                    else
                        queue.addLast(nl); // Si es en anchura agrega al final
                    //System.out.println("Agregé un nuevo camino con "+nl.size()+ " nodos");                    
                }
                //else System.out.println("Un nodo repetido descartado");
            }   
            if(validStates==0) deadEnds++;  // Un callejón sin salida                 
        }
                
        if(success) // Si hubo éxito
        {   
            long elapsed = System.currentTimeMillis()-startTime;
            if(dept) this.setTitle("16-Puzzle (Deep-First Search)");
            else this.setTitle("16-Puzzle (Breadth-First Search)"); 
            JOptionPane.showMessageDialog(rootPane, "Success!! \nPath with "+path.size()+" nodes"+"\nGenerated nodes: "+totalNodes+"\nDead ends: "+ deadEnds+"\nLoops: "+m+"\nDepth reached: " + dr +"\nElapsed time: " + elapsed + " milliseconds", 
                                                    "Good News!!!", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Success!");
            String thePath="";
            int n=0;
            int i=startState.getI();
            int j=startState.getJ();
            for(State st: path)
            {
              st.show();
              if(n>0)
                thePath = thePath+st.getMovement();                 
              n++;
            }
            Executor exec = new Executor(jBoard,i,j,thePath, empty);
            exec.start();
        }  
        else 
        { 
            JOptionPane.showMessageDialog(rootPane, "Path not found", "Sorry!!!", JOptionPane.WARNING_MESSAGE);
            System.out.println("Path not found");
        }
    }
    private void muestraEstados(ArrayList<State> ruta)
    {
        System.out.println("======");
        for(State s: ruta)
            s.show();
        System.out.println("======");
            
    }
    
    // Compara para evitar nodos repetidos
    public boolean repetido(ArrayList<State> l, State s)
    {
        boolean exist = false;
        for(State ns: l)
        {
            if(ns.isEqual(s)) // Un método propio para compaarar estados
            {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public boolean buscar(State state){
        // Si el estado que llego, esta dentro del hash map estados Visitados. Regresa que si esta (true)
        
        if(estadosVisitados.contains(state)){
            return true;
        }
        else{
            // Si no lo esta, a;adelo 
            estadosVisitados.add(state);
            // for(State succ : state.nextStates())
            //     buscar(l, succ);
            return false;
        }
    }

}
