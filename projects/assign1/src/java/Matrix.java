import java.util.*;
import java.text.*;
import java.lang.*;
import java.time.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Matrix {
    public static double onMult(int m_ar, int m_br) {
        double temp;
        int i, j, k;

        double[] pha, phb, phc;

        pha = new double[m_ar * m_ar];
        phb = new double[m_ar * m_ar];
        phc = new double[m_ar * m_ar];
        
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                pha[i * m_ar + j] = (double) 1.0;
            }
        }

        for (i = 0; i < m_br; i++) {
            for (j = 0; j < m_br; j++) {
                phb[i * m_br + j] = (double) (i + 1);
            }
        }

        double Time1 = System.nanoTime();
        
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                temp = 0;
                for (k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        double Time2 = System.nanoTime();

        // Calculate the time it took to perform the multiplication
        double Total =  ((Time2 - Time1) / 1e9);
        
        // Display the first 10 elements of the resulting matrix phc
        System.out.println("Result matrix: ");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
        System.out.println("");
        
        pha = null;
        phb = null;
        phc = null;

        return Total;
    }

    public static double onMultLine(int m_ar, int m_br) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];
        double temp;
        int i, j, k;

        // Initialize pha with 1.0 in each element
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_ar; j++) {
                pha[i * m_ar + j] = 1.0;
            }
        }

        // Initialize phb with the value of the row index plus 1 in each element
        for (i = 0; i < m_br; i++) {
            for (j = 0; j < m_br; j++) {
                phb[i * m_br + j] = i + 1;
            }
        }

        double Time1 = System.nanoTime();

        // Perform matrix multiplication using nested loops
        for (i = 0; i < m_ar; i++) {
            for (j = 0; j < m_br; j++) {
                temp = 0;
                for (k = 0; k < m_ar; k++) {
                    phc[i * m_ar + k] += pha[i * m_ar + j] * phb[j * m_br + k];
                }
            }
        }

        double Time2 = System.nanoTime();

        // Calculate the time it took to perform the multiplication
        double Total = ((Time2 - Time1) / 1e9);

        // Display the first 10 elements of the resulting matrix phc
        System.out.println("Result matrix:");
        for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++) {
                System.out.print(phc[j] + " ");
            }
        }
        System.out.println("");

        // Free memory
        pha = null;
        phb = null;
        phc = null;

        return Total;
    }



    public static void main(String[] args) {
        char c;
        int lin, col, blockSize;
        int op;
        int size;

        double result;
        PrintWriter file;

        Scanner input = new Scanner(System.in);

        op = 1;
        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.print("Selection?: ");
            op = input.nextInt();
            if (op == 0)
                break;

            switch (op) {
                case 1:
                    try {
                        file = new PrintWriter(new FileWriter("java_dot_product_data.txt"));
                        for (size = 600; size <= 3000; size += 400) {
                            result = onMult(size, size);
                            //first column - matrix size
                            //second column - Time 1
                            //third column - Time 2
                            //fourth column - Elapsed Time
                            file.println(size + ";" + result);
                        }
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        file = new PrintWriter(new FileWriter("java_line_product_data.txt"));
                        for (size = 600; size <= 3000; size += 400) {
                            result = onMultLine(size, size);
                            //first column - matrix size
                            //second column - Time 1
                            //third column - Time 2
                            //fourth column - Elapsed Time
                            file.println(size + ";" + result);
                        }
                        file.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
        	}
        } while (op != 0);
    }
}
