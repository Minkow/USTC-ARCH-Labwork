import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * @author yannameng.qyq 2012-2015@USTC
 * 模板说明：该模板主要提供依赖Swing组件提供的JPanle，JFrame，JButton等提供的GUI。使用“监听器”模式监听各个Button的事件，从而根据具体事件执行不同方法。
 * Tomasulo算法核心需同学们自行完成，见说明（4）
 * 对于界面必须修改部分，见说明(1),(2),(3)
 *
 *  (1)说明：根据你的设计完善指令设置中的下拉框内容
 *	(2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
 *	(3)说明：设置界面默认指令
 *	(4)说明： Tomasulo算法实现
 */

public class Tomasulo extends JFrame implements ActionListener{
    /*
     * 界面上有六个面板：
     * ins_set_panel : 指令设置
     * EX_time_set_panel : 执行时间设置
     * ins_state_panel : 指令状态
     * RS_panel : 保留站状态
     * Load_panel : Load部件
     * Registers_state_panel : 寄存器状态
     */
    private JPanel ins_set_panel,EX_time_set_panel,ins_state_panel,RS_panel,Load_panel,Registers_state_panel;

    /*
     * 四个操作按钮：步进，进5步，重置，执行
     */
    private JButton stepbut,step5but,resetbut,startbut;

    /*
     * 指令选择框
     */
    private JComboBox inst_typebox[]=new JComboBox[24];

    /*
     * 每个面板的名称
     */
    private JLabel inst_typel, timel, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
    private int time[]=new int[4];

    /*
     * 部件执行时间的输入框
     */
    private JTextField tt1,tt2,tt3,tt4;

    public static int m=0;//Reg中结果的计数
    private int intv[][]=new int[6][4],cnow,inst_typenow=0;
    private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};
    private int ld[][]={{0,0},{0,0},{0,0}};
    private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    /*
     * (1)说明：根据你的设计完善指令设置中的下拉框内容
     * inst_type： 指令下拉框内容:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"…………
     * regist_table：       目的寄存器下拉框内容:"F0","F2","F4","F6","F8" …………
     * rx：       源操作数寄存器内容:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" …………
     * ix：       立即数下拉框内容:"0","1","2","3","4","5","6","7","8","9" …………
     */
    private String  inst_type[]={"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D","BNE"},
            regist_table[]={"F0","F2","F4","F6","F8","F10","F12","F14","F16"
                    ,"F18","F20","F22","F24","F26","F28","F30","F32"},
            rx[]={"R0","R1","R2","R3","R4","R5","R6"},
            ix[]={"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};

    /*
     * (2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
     * 		指令状态 面板
     * 		保留站 面板
     * 		Load部件 面板
     * 		寄存器 面板
     * 					的大小
     */
    private	String  my_inst_type[][]=new String[7][4], my_rs[][]=new String[6][8],
            my_load[][]=new String[4][4], my_regsters[][]=new String[3][17];
    private	JLabel  inst_typejl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
            ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];

    //构造方法
    public Tomasulo() {
        super("Tomasulo Simulator");

        //设置布局
        Container cp = getContentPane();
        FlowLayout layout = new FlowLayout();
        cp.setLayout(layout);

        //指令设置。GridLayout(int 指令条数, int 操作码+操作数, int hgap, int vgap)
        inst_typel = new JLabel("指令设置");
        ins_set_panel = new JPanel(new GridLayout(6, 4, 0, 0));
        ins_set_panel.setPreferredSize(new Dimension(350, 150));
        ins_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        //操作按钮:执行，重设，步进，步进5步
        timel = new JLabel("执行时间设置");
        EX_time_set_panel = new JPanel(new GridLayout(2, 4, 0, 0));
        EX_time_set_panel.setPreferredSize(new Dimension(280, 80));
        EX_time_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        //指令状态
        insl = new JLabel("指令状态");
        ins_state_panel = new JPanel(new GridLayout(7, 4, 0, 0));
        ins_state_panel.setPreferredSize(new Dimension(420, 175));
        ins_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));


        //寄存器状态
        regl = new JLabel("寄存器");
        Registers_state_panel = new JPanel(new GridLayout(3, 17, 0, 0));
        Registers_state_panel.setPreferredSize(new Dimension(740, 75));
        Registers_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        //保留站
        resl = new JLabel("保留站");
        RS_panel = new JPanel(new GridLayout(6, 7, 0, 0));
        RS_panel.setPreferredSize(new Dimension(420, 150));
        RS_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        //Load部件
        ldl = new JLabel("Load部件");
        Load_panel = new JPanel(new GridLayout(4, 4, 0, 0));
        Load_panel.setPreferredSize(new Dimension(200, 100));
        Load_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        tl1 = new JLabel("Load");
        tl2 = new JLabel("加/减");
        tl3 = new JLabel("乘法");
        tl4 = new JLabel("除法");

//操作按钮:执行，重设，步进，步进5步
        stepsl = new JLabel();
        stepsl.setPreferredSize(new Dimension(200, 30));
        stepsl.setHorizontalAlignment(SwingConstants.CENTER);
        stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        stepbut = new JButton("步进");
        stepbut.addActionListener(this);
        step5but = new JButton("步进5步");
        step5but.addActionListener(this);
        startbut = new JButton("执行");
        startbut.addActionListener(this);
        resetbut = new JButton("重设");
        resetbut.addActionListener(this);
        tt1 = new JTextField("2");
        tt2 = new JTextField("2");
        tt3 = new JTextField("10");
        tt4 = new JTextField("40");

//指令设置
		/*
		 * 设置指令选择框（操作码，操作数，立即数等）的default选择
		 */
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++) {
                if (j == 0) {
                    inst_typebox[i * 4 + j] = new JComboBox(inst_type);
                } else if (j == 1) {
                    inst_typebox[i * 4 + j] = new JComboBox(regist_table);
                } else if (j == 2) {
                    inst_typebox[i * 4 + j] = new JComboBox(ix);
                } else {
                    inst_typebox[i * 4 + j] = new JComboBox(rx);
                }
                inst_typebox[i * 4 + j].addActionListener(this);
                ins_set_panel.add(inst_typebox[i * 4 + j]);
            }
        for (int i = 2; i < 6; i++)
            for (int j = 0; j < 4; j++) {
                if (j == 0) {
                    inst_typebox[i * 4 + j] = new JComboBox(inst_type);
                } else {
                    inst_typebox[i * 4 + j] = new JComboBox(regist_table);
                }
                inst_typebox[i * 4 + j].addActionListener(this);
                ins_set_panel.add(inst_typebox[i * 4 + j]);
            }

		/*
		 * (3)说明：设置界面默认指令，根据你设计的指令，操作数等的选择范围进行设置。
		 * 默认6条指令。待修改
		 */
        inst_typebox[0].setSelectedIndex(1);
        inst_typebox[1].setSelectedIndex(3);
        inst_typebox[2].setSelectedIndex(21);
        inst_typebox[3].setSelectedIndex(2);
        /*L.D F2,20(R3)*/
        inst_typebox[4].setSelectedIndex(1);
        inst_typebox[5].setSelectedIndex(1);
        inst_typebox[6].setSelectedIndex(20);
        inst_typebox[7].setSelectedIndex(3);
        /*MUL.D F0,F2,F4*/
        inst_typebox[8].setSelectedIndex(4);
        inst_typebox[9].setSelectedIndex(0);
        inst_typebox[10].setSelectedIndex(1);
        inst_typebox[11].setSelectedIndex(2);
        /*SUB.D F8,F6,F2*/
        inst_typebox[12].setSelectedIndex(3);
        inst_typebox[13].setSelectedIndex(4);
        inst_typebox[14].setSelectedIndex(3);
        inst_typebox[15].setSelectedIndex(1);
        /*DIV.D F10,F0,F6*/
        inst_typebox[16].setSelectedIndex(5);
        inst_typebox[17].setSelectedIndex(5);
        inst_typebox[18].setSelectedIndex(0);
        inst_typebox[19].setSelectedIndex(3);
        /*ADD.D F6,F8,F2*/
        inst_typebox[20].setSelectedIndex(2);
        inst_typebox[21].setSelectedIndex(3);
        inst_typebox[22].setSelectedIndex(4);
        inst_typebox[23].setSelectedIndex(1);

//执行时间设置
        EX_time_set_panel.add(tl1);
        EX_time_set_panel.add(tt1);
        EX_time_set_panel.add(tl2);
        EX_time_set_panel.add(tt2);
        EX_time_set_panel.add(tl3);
        EX_time_set_panel.add(tt3);
        EX_time_set_panel.add(tl4);
        EX_time_set_panel.add(tt4);

//指令状态设置
        for (int i=0;i<7;i++)
        {
            for (int j=0;j<4;j++){
                inst_typejl[i][j]=new JLabel(my_inst_type[i][j]);
                inst_typejl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                ins_state_panel.add(inst_typejl[i][j]);
            }
        }
//保留站设置
        for (int i=0;i<6;i++)
        {
            for (int j=0;j<8;j++){
                resjl[i][j]=new JLabel(my_rs[i][j]);
                resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                RS_panel.add(resjl[i][j]);
            }
        }
//Load部件设置
        for (int i=0;i<4;i++)
        {
            for (int j=0;j<4;j++){
                ldjl[i][j]=new JLabel(my_load[i][j]);
                ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                Load_panel.add(ldjl[i][j]);
            }
        }
//寄存器设置
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<17;j++){
                regjl[i][j]=new JLabel(my_regsters[i][j]);
                regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                Registers_state_panel.add(regjl[i][j]);
            }
        }

//向容器添加以上部件
        cp.add(inst_typel);
        cp.add(ins_set_panel);
        cp.add(timel);
        cp.add(EX_time_set_panel);

        cp.add(startbut);
        cp.add(resetbut);
        cp.add(stepbut);
        cp.add(step5but);

        cp.add(Load_panel);
        cp.add(ldl);
        cp.add(RS_panel);
        cp.add(resl);
        cp.add(stepsl);
        cp.add(Registers_state_panel);
        cp.add(regl);
        cp.add(ins_state_panel);
        cp.add(insl);

        stepbut.setEnabled(false);
        step5but.setEnabled(false);
        ins_state_panel.setVisible(false);
        insl.setVisible(false);
        RS_panel.setVisible(false);
        ldl.setVisible(false);
        Load_panel.setVisible(false);
        resl.setVisible(false);
        stepsl.setVisible(false);
        Registers_state_panel.setVisible(false);
        regl.setVisible(false);
        setSize(820,750);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public class Instruction{
        String type;
        String opr1;
        String opr2;
        String opr3;
    }

    public class InstrStation{
        String name;
        int state;
        int excutetime;
        Instruction instruction;
    }

    public class LoadStation{
        String name;
        String Busy;
        String Addr;
        String Value;
    }

    public class RegStation{
        String name;
        String Value;
        String state;
    }

    public class RsStation{
        String name;
        String Vj;
        String Vk;
        String Qj;
        String Qk;
        String Op;
        String Busy;
    }

    private Instruction instruction[] = new Instruction[6];
    private InstrStation IS[] = new InstrStation[6];
    private RsStation RS[] = new RsStation[5];
    private LoadStation LS[] = new LoadStation[3];
    private RegStation RegS[] = new RegStation[16];

    public int Extime(Instruction instruction){
        if(instruction.type=="L.D")
        {
            return Integer.parseInt(tt1.getText());
        }
        else if(instruction.type=="ADD.D" || instruction.type=="SUB.D")
        {
            return Integer.parseInt(tt2.getText());
        }
        else if(instruction.type=="MULT.D")
        {
            return Integer.parseInt(tt3.getText());
        }
        else if(instruction.type=="DIV.D")
        {
            return Integer.parseInt(tt4.getText());
        }
        else {
            return 0;
        }

    }

    /*
     * 点击”执行“按钮后，根据选择的指令，初始化其他几个面板
     * 顺便也要把自己定义的数据结构初始化
     */

    public void init(){
        // get Value
		/*intv：6行4列的整型数组*/
        for (int i=0;i<6;i++){
            intv[i][0]=inst_typebox[i*4].getSelectedIndex();
            if (intv[i][0]!=0){
                intv[i][1]=2*inst_typebox[i*4+1].getSelectedIndex();
				/*指令形式为load时，选择列表为fx,ix,rx*/
                if (intv[i][0]==1){
                    intv[i][2]=inst_typebox[i*4+2].getSelectedIndex();
                    intv[i][3]=inst_typebox[i*4+3].getSelectedIndex();
                }
				/*指令形式为算术运算指令时，选择列表为fx,regist_table,regist_table*/
                else {
                    intv[i][2]=2*inst_typebox[i*4+2].getSelectedIndex();
                    intv[i][3]=2*inst_typebox[i*4+3].getSelectedIndex();
                }
            }
        }
		/*获取文本框中字符串
		 * 为指令执行周期
		 * */
        time[0]=Integer.parseInt(tt1.getText());
        time[1]=Integer.parseInt(tt2.getText());
        time[2]=Integer.parseInt(tt3.getText());
        time[3]=Integer.parseInt(tt4.getText());
        //System.out.println(time[0]);
        // set 0
        my_inst_type[0][0]="指令";
        my_inst_type[0][1]="流出";
        my_inst_type[0][2]="执行";
        my_inst_type[0][3]="写回";


        my_load[0][0]="名称";
        my_load[0][1]="Busy";
        my_load[0][2]="地址";
        my_load[0][3]="值";
        my_load[1][0]="Load1";
        my_load[2][0]="Load2";
        my_load[3][0]="Load3";
        my_load[1][1]="no";
        my_load[2][1]="no";
        my_load[3][1]="no";

        my_rs[0][0]="Time";
        my_rs[0][1]="名称";
        my_rs[0][2]="Busy";
        my_rs[0][3]="Op";
        my_rs[0][4]="Vj";
        my_rs[0][5]="Vk";
        my_rs[0][6]="Qj";
        my_rs[0][7]="Qk";
        my_rs[1][1]="Add1";
        my_rs[2][1]="Add2";
        my_rs[3][1]="Add3";
        my_rs[4][1]="Mult1";
        my_rs[5][1]="Mult2";
        my_rs[1][2]="no";
        my_rs[2][2]="no";
        my_rs[3][2]="no";
        my_rs[4][2]="no";
        my_rs[5][2]="no";

        my_regsters[0][0]="字段";
        for (int i=1;i<17;i++){
            //System.out.print(i+" "+regist_table[i-1];
            my_regsters[0][i]=regist_table[i-1];

        }
        my_regsters[1][0]="状态";
        my_regsters[2][0]="值";

        for (int i=1;i<7;i++)
        {
            instruction[i-1] = new Instruction();
            for (int j=0;j<4;j++){
                if (j==0){
                    int temp=i-1;
                    String disp;
                    disp = inst_type[inst_typebox[temp*4].getSelectedIndex()]+" ";
                    if (inst_typebox[temp*4].getSelectedIndex()==0) { //null
                        instruction[i-1].type=inst_type[inst_typebox[temp*4].getSelectedIndex()];
                        instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
                        instruction[i-1].opr2=regist_table[inst_typebox[temp*4+2].getSelectedIndex()];
                        instruction[i-1].opr3=regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
                    }
                    else if (inst_typebox[temp*4].getSelectedIndex()==1){//load
                        disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+ix[inst_typebox[temp*4+2].getSelectedIndex()]+'('+rx[inst_typebox[temp*4+3].getSelectedIndex()]+')';
                        instruction[i-1].type=inst_type[inst_typebox[temp*4].getSelectedIndex()];
                        instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
                        instruction[i-1].opr2=ix[inst_typebox[temp*4+2].getSelectedIndex()];
                        instruction[i-1].opr3=rx[inst_typebox[temp*4+3].getSelectedIndex()];
                    }
                    else { //cal
                        disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+2].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
                        instruction[i-1].type=inst_type[inst_typebox[temp*4].getSelectedIndex()];
                        instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
                        instruction[i-1].opr2=regist_table[inst_typebox[temp*4+2].getSelectedIndex()];
                        instruction[i-1].opr3=regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
                    }
                    my_inst_type[i][j]=disp;
                }
                else my_inst_type[i][j]="";
            }

            IS[i-1]=new InstrStation();
            IS[i-1].state=0;
            IS[i-1].instruction=instruction[i-1];
            IS[i-1].excutetime=Extime(instruction[i-1]);
        }

        for (int i=1;i<6;i++)
            for (int j=0;j<8;j++)if (j!=1&&j!=2){
                my_rs[i][j]="";
            }
        for (int i=1;i<4;i++)
            for (int j=2;j<4;j++){
                my_load[i][j]="";
            }
        for (int i=1;i<3;i++)
            for (int j=1;j<17;j++){
                my_regsters[i][j]="";
            }
        inst_typenow=0;

        for (int i=0;i<5;i++){
            for (int j=1;j<3;j++) cal[i][j]=0;
            cal[i][0]=-1;
        }
        for (int i=0;i<3;i++)
            for (int j=0;j<2;j++) ld[i][j]=0;
        for (int i=0;i<17;i++) ff[i]=0;


        for(int i=0;i<5;i++)
        {
            RS[i]=new RsStation();
            RS[i].name=my_rs[i+1][1];
            RS[i].Busy=my_rs[i+1][2];
            RS[i].Op=my_rs[i+1][3];
            RS[i].Vj=my_rs[i+1][4];
            RS[i].Vk=my_rs[i+1][5];
            RS[i].Qj=my_rs[i+1][6];
            RS[i].Qk=my_rs[i+1][7];
        }
        for(int i=0;i<3;i++)
        {
            LS[i]=new LoadStation();
            LS[i].name=my_load[i+1][0];
            LS[i].Busy=my_load[i+1][1];
            LS[i].Addr=my_load[i+1][2];
            LS[i].Value=my_load[i+1][3];
        }
        for(int i=0;i<16;i++)
        {
            RegS[i]=new RegStation();
            RegS[i].state=my_regsters[0][i+1];
            RegS[i].name=my_regsters[1][i+1];
            RegS[i].Value=my_regsters[2][i+1];
        }


    }
    /*
     * 点击操作按钮后，用于显示结果
     */
    public void display(){
        for (int i=0;i<7;i++)
            for (int j=0;j<4;j++){
                inst_typejl[i][j].setText(my_inst_type[i][j]);
            }
        for (int i=0;i<6;i++)
            for (int j=0;j<8;j++){
                resjl[i][j].setText(my_rs[i][j]);
            }
        for (int i=0;i<4;i++)
            for (int j=0;j<4;j++){
                ldjl[i][j].setText(my_load[i][j]);
            }
        for (int i=0;i<3;i++)
            for (int j=0;j<17;j++){
                regjl[i][j].setText(my_regsters[i][j]);
            }
        stepsl.setText("当前周期："+String.valueOf(cnow-1));
    }

    public void actionPerformed(ActionEvent e){
//点击“执行”按钮的监听器
        if (e.getSource()==startbut) {
            for (int i=0;i<24;i++) inst_typebox[i].setEnabled(false);
            tt1.setEnabled(false);tt2.setEnabled(false);
            tt3.setEnabled(false);tt4.setEnabled(false);
            stepbut.setEnabled(true);
            step5but.setEnabled(true);
            startbut.setEnabled(false);
            //根据指令设置的指令初始化其他的面板
            init();
            cnow=1;
            //展示其他面板
            display();
            ins_state_panel.setVisible(true);
            RS_panel.setVisible(true);
            Load_panel.setVisible(true);
            Registers_state_panel.setVisible(true);
            insl.setVisible(true);
            ldl.setVisible(true);
            resl.setVisible(true);
            stepsl.setVisible(true);
            regl.setVisible(true);
        }
//点击“重置”按钮的监听器
        if (e.getSource()==resetbut) {
            for (int i=0;i<24;i++) inst_typebox[i].setEnabled(true);
            tt1.setEnabled(true);tt2.setEnabled(true);
            tt3.setEnabled(true);tt4.setEnabled(true);
            stepbut.setEnabled(false);
            step5but.setEnabled(false);
            startbut.setEnabled(true);
            ins_state_panel.setVisible(false);
            insl.setVisible(false);
            RS_panel.setVisible(false);
            ldl.setVisible(false);
            Load_panel.setVisible(false);
            resl.setVisible(false);
            stepsl.setVisible(false);
            Registers_state_panel.setVisible(false);
            regl.setVisible(false);
        }
//点击“步进”按钮的监听器
        if (e.getSource()==stepbut) {
            core();
            cnow++;
            display();
        }
//点击“进5步”按钮的监听器
        if (e.getSource()==step5but) {
            for (int i=0;i<5;i++){
                core();
                cnow++;
            }
            display();
        }

        for (int i=0;i<24;i=i+4)
        {
            if (e.getSource()==inst_typebox[i]) {
                if (inst_typebox[i].getSelectedIndex()==1){
                    inst_typebox[i+2].removeAllItems();
                    for (int j=0;j<ix.length;j++) inst_typebox[i+2].addItem(ix[j]);
                    inst_typebox[i+3].removeAllItems();
                    for (int j=0;j<rx.length;j++) inst_typebox[i+3].addItem(rx[j]);
                }
                else {
                    inst_typebox[i+2].removeAllItems();
                    for (int j=0;j<regist_table.length;j++) inst_typebox[i+2].addItem(regist_table[j]);
                    inst_typebox[i+3].removeAllItems();
                    for (int j=0;j<regist_table.length;j++) inst_typebox[i+3].addItem(regist_table[j]);
                }
            }
        }
    }
    /*
     * (4)说明： Tomasulo算法实现
     */

    private int Lidle(LoadStation LS[]){
        int idle=-1;
        for(int i=0;i<LS.length;i++)
        {
            if(LS[i].Busy=="no")
            {
                idle=i;
                break;
            }
        }
        return idle;
    }

    private int Rsidle(InstrStation IS,RsStation RS[]) {
        int idle=-1;
        if(IS.instruction.type=="ADD.D" || IS.instruction.type=="SUB.D")
        {
            for(int i=0;i<3;i++)
            {
                if(RS[i].Busy=="no")
                {
                    idle=i;
                    break;
                }
            }
        }
        else if(IS.instruction.type=="MULT.D" || IS.instruction.type=="DIV.D")
        {
            for(int i=3;i<5;i++)
            {
                if(RS[i].Busy=="no")
                {
                    idle=i;
                    break;
                }
            }
        }
        return idle;
    }

    private int ISline(InstrStation IS[]) {
        int n=-1;
        for(int i=0;i<IS.length;i++)
        {
            if(IS[i].state==0 && IS[i].name!="NOP")
            {
                n=i;
                break;
            }
        }
        return n;
    }

    private int[] Stateline(InstrStation IS[],int m) {
        int n=0;
        for(int i=0;i<IS.length;i++)
        {
            if(IS[i].state==m && IS[i].name!="NOP")
            {
                n++;
            }//get length of this state instrs
        }
        int line[] = new int[n];
        for(int i=0;i<n;i++)
        {
            line[i]=-1;
        }//init
        for(int i=0,j=0;i<IS.length;i++)
        {
            if(IS[i].state==m && IS[i].name!="NOP")
            {
                line[j]=i;
                j++;
            }
        }
        return line;
    }

    public void core()
    {
        int is,ex1[],ex2[],wb[];
        is=this.ISline(IS);//state0
        ex1=this.Stateline(IS,1);//state1
        ex2=this.Stateline(IS,2);//state2
        wb=this.Stateline(IS,3);//state3

        if(is!=-1)  //issue
        {
            InstrStation ins=IS[is];
            if(ins.instruction.type=="L.D") //load
            {
                int ldid;
                ldid=this.Lidle(LS); //find no busy load station
                if(ldid!=-1)
                {
                    ins.name=LS[ldid].name;
                    LS[ldid].Busy="yes";
                    LS[ldid].Addr=ins.instruction.opr2;

                    my_load[ldid+1][1]=LS[ldid].Busy;
                    my_load[ldid+1][2]=LS[ldid].Addr;
                }
            }

            else if(ins.instruction.type!="BEQ"){
                int rsid;
                boolean flag1,flag2;
                rsid=this.Rsidle(IS[is], RS);
                if(rsid!=-1)
                {
                    ins.name=RS[rsid].name;
                    RS[rsid].Busy="yes";
                    RS[rsid].Op=ins.instruction.type;

                    my_rs[rsid+1][2]=RS[rsid].Busy;
                    my_rs[rsid+1][3]=RS[rsid].Op;
                    //非busy的保留站
                    flag1=false;
                    flag2=false;
                    for(int i=0;i<is;i++)
                    {
                        String destination;
                        destination=IS[i].instruction.opr1;
                        //循环检测有没有相关的寄存器
                        if(ins.instruction.opr2==destination)
                        {
                            flag1=true;
                            for(int j=0;j<RegS.length;j++)
                            {
                                if(RegS[j].state==destination)
                                {
                                    if(RegS[j].Value=="")
                                    {
                                        RS[rsid].Qj=RegS[j].name;
                                        my_rs[rsid+1][6]=RS[rsid].Qj; //源操作数未准备好
                                    }
                                    else {
                                        RS[rsid].Vj=RegS[j].Value;
                                        my_rs[rsid+1][4]=RS[rsid].Vj;
                                    }
                                }
                            }
                        }
                        if(ins.instruction.opr3==destination)
                        {
                            flag2=true;
                            for(int j=0;j<RegS.length;j++)
                            {
                                if(RegS[j].state==destination)
                                {
                                    if(RegS[j].Value=="")
                                    {
                                        RS[rsid].Qk=RegS[j].name;
                                        my_rs[rsid+1][7]=RS[rsid].Qk; //源操作数未准备好
                                    }
                                    else {
                                        RS[rsid].Vk=RegS[j].Value;
                                        my_rs[rsid+1][5]=RS[rsid].Vk;
                                    }
                                }
                            }
                        }
                    }
                    if(!flag1)
                    {
                        RS[rsid].Vj=ins.instruction.opr2;
                        my_rs[rsid+1][4]="R["+RS[rsid].Vj+"]";
                    }
                    if(!flag2)
                    {
                        RS[rsid].Vk=ins.instruction.opr3;
                        my_rs[rsid+1][5]="R["+RS[rsid].Vk+"]";
                    }
                }
            }
            //修改RegStation
            String destination0,name;
            destination0=ins.instruction.opr1;
            name=ins.name;
            for(int i=0;i<this.RegS.length;i++)
            {
                if(RegS[i].state==destination0)
                {
                    RegS[i].name=name; //该Reg为name保留站的目标寄存器
                    my_regsters[1][i+1]=RegS[i].name;
                    break;
                }
            }
            my_inst_type[is+1][1]=String.valueOf(cnow); //issue time为cnow
            IS[is].state=1; //等待执行
        }

        for(int i=0;i<ex1.length;i++) //等待执行(其实只是为了填个执行开始时间和"-"
        {
            if(ex1[i]!=-1)
            {
                InstrStation exline=IS[ex1[i]];
                if(exline.instruction.type=="L.D")
                {
                    for(int j=0;j<LS.length;j++)
                    {
                        if(LS[j].name==exline.name)
                        {
                            LS[j].Addr="R["+exline.instruction.opr3+"]"+exline.instruction.opr2;
                            my_load[j+1][2]=LS[j].Addr;
                            exline.excutetime--;
                            break;
                        }
                    }
                    my_inst_type[ex1[i]+1][2]=String.valueOf(cnow)+"-";
                    IS[ex1[i]].state=2; //转入执行
                }

                else { //cal
                    for(int j=0;j<RS.length;j++)
                    {
                        if(RS[j].name==exline.name)
                        {
                            if(!RS[j].Vj.equals("") && !RS[j].Vk.equals(""))
                            {
                                exline.excutetime--;
                                my_rs[j+1][0]=String.valueOf(exline.excutetime);
                                my_inst_type[ex1[i]+1][2]=String.valueOf(cnow)+"-";
                                IS[ex1[i]].state=2;
                                break;
                            }
                        }
                    }
                }
            }
        }

        for(int i=0;i<ex2.length;i++) //exing
        {
            if(ex2[i]!=-1)
            {
                InstrStation exing=IS[ex2[i]];
                if(exing.instruction.type=="L.D")
                {
                    for(int j=0;j<LS.length;j++)
                    {
                        if(LS[j].name==exing.name)
                        {
                            LS[j].Value="M["+LS[j].Addr+"]";
                            my_load[j+1][3]=LS[j].Value;
                            exing.excutetime--;
                            break;
                        }
                    }
                    if(exing.excutetime==0)
                    {
                        my_inst_type[ex2[i]+1][2]+=String.valueOf(cnow);
                        IS[ex2[i]].state=3; //wb
                    }
                }

                else { //cal
                    int j;
                    for(j=0;j<RS.length;j++)
                    {
                        if(RS[j].name==exing.name)
                        {
                            exing.excutetime--;
                            my_rs[j+1][0]=String.valueOf(exing.excutetime);
                            break;
                        }
                    }
                    if(exing.excutetime==0)
                    {
                        my_inst_type[ex2[i]+1][2]+=String.valueOf(cnow);
                        IS[ex2[i]].state=3;
                        my_rs[j+1][0]="";
                    }
                }
            }
        }
        
        for(int i=0;i<wb.length;i++) //wb
        {
            if(wb[i]!=-1)
            {
                InstrStation wbline=IS[wb[i]];
                String name0=wbline.name;
 
                if(wbline.instruction.type=="L.D")
                {
                    for(int j=0;j<LS.length;j++)
                    {
                        if(LS[j].name==wbline.name)
                        {
                            LS[j].Busy="no";
                            LS[j].Addr="";
                            LS[j].Value="";
                            my_load[j+1][1]=LS[j].Busy;
                            my_load[j+1][2]=LS[j].Addr;
                            my_load[j+1][3]=LS[j].Value;
                            break;
                        }
                    }
                }
                else
                {
                    for(int j=0;j<RS.length;j++)
                    {
                        if(RS[j].name==name0)
                        {
                            RS[j].Busy="no";
                            RS[j].Op="";
                            RS[j].Qj="";
                            RS[j].Qk="";
                            RS[j].Vj="";
                            RS[j].Vk="";
                            my_rs[j+1][2]=RS[j].Busy;
                            for(int k=3;k<8;k++)
                                my_rs[j+1][k]=""; //--nobusy
                            break;
                        }
                    }
                }
                for(int j=0;j<RegS.length;j++) //更新Reg
                {
                    if(RegS[j].name==name0)
                    {
                        m++;
                        RegS[j].Value="M"+m;
                        my_regsters[2][j+1]=RegS[j].Value;
                    }
                }
                for(int j=0;j<RS.length;j++) //更新保留站
                {
                    if(RS[j].Qj==name0)
                    {
                        RS[j].Vj="M"+m;
                        RS[j].Qj="";
                        my_rs[j+1][4]=RS[j].Vj;
                        my_rs[j+1][6]=RS[j].Qj;
                    }
                    if(RS[j].Qk==name0)
                    {
                        RS[j].Vk="M"+m;
                        RS[j].Qk="";
                        my_rs[j+1][5]=RS[j].Vk;
                        my_rs[j+1][7]=RS[j].Qk;
                    }
                }
                my_inst_type[wb[i]+1][3]=String.valueOf(cnow);
                IS[wb[i]].state=4;
            }
        }

        boolean complete=true;
        for(int l=0;l<IS.length;l++)
        {
            if(IS[l].instruction.type!="NOP" && my_inst_type[l+1][3]=="")
            {
                complete=false;
                break;
            }
        }
        if(complete)
        {
            stepbut.setEnabled(false);
            step5but.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        new Tomasulo();
    }
}
