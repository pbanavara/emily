package co.axoni.classify; /**
 * Created by pbanavara on 07/12/15.
 */

import libsvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

public class SvmPredict {
    private final static Logger logger = LoggerFactory.getLogger(SvmPredict.class);
    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s) {}
    };

    private static svm_print_interface svm_print_stdout = new svm_print_interface()
    {
        public void print(String s)
        {
            System.out.print(s);
        }
    };

    private static svm_print_interface svm_print_string = svm_print_stdout;

    static void info(String s)
    {
        svm_print_string.print(s);
    }

    private static double atof(String s)
    {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    private static String predict(String inputStr, svm_model model, int predict_probability) throws IOException
    {
        StringBuilder output = new StringBuilder();
        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        int svm_type=svm.svm_get_svm_type(model);
        int nr_class=svm.svm_get_nr_class(model);
        double[] prob_estimates=null;

        if(predict_probability == 1)
        {
            if(svm_type == svm_parameter.EPSILON_SVR ||
                    svm_type == svm_parameter.NU_SVR)
            {
                //svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
            }
            else
            {
                int[] labels=new int[nr_class];
                svm.svm_get_labels(model,labels);
                prob_estimates = new double[nr_class];
                //output.writeBytes("labels");
                output.append("labels");
                for(int j=0;j<nr_class;j++)
                 //   output.writeBytes(" "+labels[j]);
                    output.append(" "+labels[j]).append("\n");
                //output.writeBytes("\n");
            }
        }
            String line = inputStr;
            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            double target = atof(st.nextToken());
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int j=0;j<m;j++)
            {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }

            double v;
            if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
            {
                v = svm.svm_predict_probability(model,x,prob_estimates);
                //output.writeBytes(v+" ");
                output.append(v + " ");

                for(int j=0;j<nr_class;j++) {
                    //output.writeBytes(prob_estimates[j]+" ");
                    //output.writeBytes("\n");
                    output.append(prob_estimates[j]+" ").append("\n");
                }

            }
            else
            {
                v = svm.svm_predict(model,x);
                //output.writeBytes(v+"\n");
                output.append(v).append("\n");
            }

            if(v == target)
                ++correct;
            error += (v-target)*(v-target);
            sumv += v;
            sumy += target;
            sumvv += v*v;
            sumyy += target*target;
            sumvy += v*target;
            ++total;
        if(svm_type == svm_parameter.EPSILON_SVR ||
                svm_type == svm_parameter.NU_SVR) {
            /*
            svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
            svm_predict.info("Squared correlation coefficient = "+
                    ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
                            ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
                    " (regression)\n");
                    */
            return output.toString();
        }
        else {
            /*
            svm_predict.info("Accuracy = "+(double)correct/total*100+
                    "% ("+correct+"/"+total+") (classification)\n");
            */
            logger.debug("OUTPUT :::" + output.toString());
            return output.toString();
        }
    }

    private static void exit_with_help()
    {
        System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
                +"options:\n"
                +"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
                +"-q : quiet mode (no outputs)\n");
        System.exit(1);
    }

    public static String predictEmail(String input, String modelFileName) throws IOException
    {
        String returnVal = "";
        int predict_probability=0;
        try
        {
            svm_model model = svm.svm_load_model(modelFileName);
            if (model == null)
            {
                logger.debug("can't open model file "+modelFileName);
            }
            else {
                returnVal =  predict(input, model, predict_probability);
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return returnVal;


    }
}

