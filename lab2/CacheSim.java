import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;
import java.io.FileReader;
import java.lang.*;


public class CacheSim extends JFrame implements ActionListener {

    private JPanel panelTop, panelLeft, panelRight, panelBottom;
    private JButton execStepBtn, execAllBtn, fileBotton;
    private JComboBox csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
    private JComboBox icsBox, dcsBox;
    private JFileChooser fileChooser;

    private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn,csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
    private JLabel icsLabel, dcsLabel;
    private JLabel resultTagLabel[][];
    private JLabel resultDataLabel[][];

    private JLabel accessTypeTagLabel, addressTagLabel, blockNumberTagLabel, indexTagLabel, inblockAddressTagLabel, hitTagLabel, includelabel;
    private JLabel accessTypeDataLabel, addressDataLabel, blockNumberDataLabel,  indexDataLabel, inblockAddressDataLabel, hitDataLabel;

    private JRadioButton unifiedCacheButton, separateCacheButton;
    /*
        options section
    */
    private String cachesize[] = { "2KB","4KB","8KB","16KB","32KB","64KB","128KB","256KB","512KB","1MB","2MB" };
    private String scachesize[] = {"1KB","2KB","4KB","8KB","16KB","32KB","64KB","128KB","256KB","512KB","1MB"  };
    private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
    private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
    private String replace[] = { "LRU", "FIFO", "RAND" };
    private String pref[] = { "不预取", "不命中预取" };
    private String write[] = { "写回法", "写直达法" };
    private String alloc[] = { "按写分配", "不按写分配" };
    private String typename[] = { "读数据", "写数据", "读指令" };
    private String hitname[] = {"不命中", "命中" };

    private final  String PrintTagLabel[][] = {
            {"访问总次数:", "不命中次数:", "不命中率:"},
            {"读指令次数:", "不命中次数:", "不命中率:"},
            {"读数据次数:", "不命中次数:", "不命中率:"},
            {"写数据次数:", "不命中次数:", "不命中率:"}
    };


    private File file;

    private int csIndex, bsIndex, wayIndex, replaceIndex, writeIndex, allocIndex, icsIndex, dcsIndex, prefetchIndex;

    private int cacheType;

    private class Instruction {
        int op;
        int tag;
        int index;
        int blockAddr;
        int inblockAddr;
        String addr;

        private String Hex2Bin() {
            StringBuffer buffer = new StringBuffer();
            int zero = 8 - this.addr.length();
            for (int i = 0; i < zero; i++) {
                buffer.append("0000");
            }
            for (int i = 0; i < this.addr.length(); i++) {
                switch(this.addr.charAt(i)) {
                    case '0':
                        buffer.append("0000");
                        break;
                    case '1':
                        buffer.append("0001");
                        break;
                    case '2':
                        buffer.append("0010");
                        break;
                    case '3':
                        buffer.append("0011");
                        break;
                    case '4':
                        buffer.append("0100");
                        break;
                    case '5':
                        buffer.append("0101");
                        break;
                    case '6':
                        buffer.append("0110");
                        break;
                    case '7':
                        buffer.append("0111");
                        break;
                    case '8':
                        buffer.append("1000");
                        break;
                    case '9':
                        buffer.append("1001");
                        break;
                    case 'a':
                        buffer.append("1010");
                        break;
                    case 'b':
                        buffer.append("1011");
                        break;
                    case 'c':
                        buffer.append("1100");
                        break;
                    case 'd':
                        buffer.append("1101");
                        break;
                    case 'e':
                        buffer.append("1110");
                        break;
                    case 'f':
                        buffer.append("1111");
                        break;
                    default:
                        JOptionPane.showMessageDialog(null,"有错误的地址输入！","MDZZ",JOptionPane.ERROR_MESSAGE);
                }
            }
            return buffer.toString();
        }

        public Instruction(int op, String addr) {
            this.op = op;
            this.addr = addr;
            String baddr = this.Hex2Bin();

            if (cacheType == 0 && uCache != null) {
                this.tag = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset - uCache.groupOffset), 2);
                this.index = Integer.parseInt(baddr.substring(32 - uCache.blockOffset - uCache.groupOffset, 32 - uCache.blockOffset), 2);
                this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - uCache.blockOffset), 2);
                this.inblockAddr = Integer.parseInt(baddr.substring(32 - uCache.blockOffset), 2);
            }
            if (cacheType == 1 && iCache != null && dCache != null) {
                if (op == 0 || op == 1) {
                    this.tag = Integer.parseInt(baddr.substring(0, 32 - dCache.blockOffset - dCache.groupOffset), 2);
                    this.index = Integer.parseInt(baddr.substring(32 - dCache.blockOffset - dCache.groupOffset, 32 - dCache.blockOffset), 2);
                    this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - dCache.blockOffset), 2);
                    this.inblockAddr = Integer.parseInt(baddr.substring(32 - dCache.blockOffset), 2);
                }
                else if (op == 2) {
                    this.tag = Integer.parseInt(baddr.substring(0, 32 - iCache.blockOffset - iCache.groupOffset), 2);
                    this.index = Integer.parseInt(baddr.substring(32 - iCache.blockOffset - iCache.groupOffset, 32 - iCache.blockOffset), 2);
                    this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - iCache.blockOffset), 2);
                    this.inblockAddr = Integer.parseInt(baddr.substring(32 - iCache.blockOffset), 2);
                }
            }
        }
    }

    private Instruction ins[];
    private int MaxIns = 10000000;
    private int icnt;
    private int ip;

    private class CacheMem {
        int tag;
        boolean dirty;
        int time;

        public CacheMem(int tag) {
            this.tag = tag;
            dirty = false;
            time = -1;
        }
    }

    private class Cache {

        private CacheMem cache[][];
        private int cacheSize;
        private int blockSize;
        private int blockNum;
        private int blockOffset;
        private int blockperGroup;
        private int groupNum;
        private int groupOffset;
        private int FIFOTime[];
        private int LRUTime[][];

        public Cache(int csize, int bsize) {
            cacheSize = csize;
            blockSize = bsize;

            blockNum = cacheSize / blockSize;
            blockOffset = log2(blockSize);
            blockperGroup = 1<<wayIndex;
            groupNum = blockNum / blockperGroup;
            groupOffset = log2(groupNum);
            cache = new CacheMem[groupNum][blockperGroup];
            FIFOTime = new int[groupNum];
            LRUTime = new int[groupNum][blockperGroup];
            
            for (int i = 0; i < groupNum; i++) {
                for (int j = 0; j < blockperGroup; j++) {
                    cache[i][j] = new CacheMem(-1);
                }
            }//初始化
        }

        public boolean read(int tag, int index, int inblockAddr) {
            for (int i = 0; i < blockperGroup; i++) {
                    if (cache[index][i].tag == tag) {
                    for(int j =0; j < blockperGroup; j++)
                    {
                        if(j != i)
                            LRUTime[index][j]++;
                        else
                            LRUTime[index][j]=0;
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean write(int tag, int index, int inblockAddr) {
            for (int i = 0; i < blockperGroup; i++) {
                if (cache[index][i].tag == tag) {
                    for(int j =0; j < blockperGroup; j++)
                    {
                        if(j != i)
                            LRUTime[index][j]++;
                        else
                            LRUTime[index][j]=0;
                    }
                    cache[index][i].dirty = true;
                    if (writeIndex == 0) {}//WB
                    else if (writeIndex == 1)//WT
                        cache[index][i].dirty = false;
                    return true;
                }
            }
            return false;
        }

        public void replace(int tag, int index) {
            if (replaceIndex == 0) {//LRU
                int lruBlock = 0;
                for (int i = 1; i<blockperGroup;i++)
                    if (LRUTime[index][lruBlock] < LRUTime[index][i])
                        lruBlock = i;
                load(tag, index, lruBlock);
            } 
            else if (replaceIndex == 1) {//FIFO
                int fifoBlock = 0;
                for (int i=1;i<blockperGroup;i++)
                    if (cache[index][fifoBlock].time > cache[index][i].time)
                        fifoBlock = i;
                load(tag, index, fifoBlock);
            } 
            else if (replaceIndex == 2) {//rand
                int randBlock = (int)(Math.random() * blockperGroup);
                load(tag, index, randBlock);
            }
        }

        private void load(int tag, int index, int groupAddr) {
            if (writeIndex == 0 && cache[index][groupAddr].dirty) {}//模拟器并表现不出WB,WT区别...
            cache[index][groupAddr].tag = tag;
            cache[index][groupAddr].dirty = false;
            cache[index][groupAddr].time = FIFOTime[index];
            FIFOTime[index]++;
        }
    }

    Cache uCache, iCache, dCache;
    
    public CacheSim(){
        super("Cache Simulator");
        fileChooser = new JFileChooser();
        draw();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == execAllBtn) {
            simExecAll();
        }
        if (e.getSource() == execStepBtn) {
            simExecStep(false);
        }
        if (e.getSource() == fileBotton){
            int fileOver = fileChooser.showOpenDialog(null);
            if (fileOver == 0) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                fileAddrBtn.setText(path);
                file = new File(path);

                initCache();
                readFile();
            }
        }
    }

    private int Cnt[][] = new int[4][2];

    private void initCache() {
        if (cacheType == 0) {
            uCache = new Cache(1<<(11+csIndex), 1<<(4+bsIndex));
            iCache = null;
            dCache = null;

        }
        else if (cacheType == 1) {
            uCache = null;
            iCache = new Cache(1<<(10+icsIndex), 1<<(4+bsIndex));
            dCache = new Cache(1<<(10+dcsIndex), 1<<(4+bsIndex));
        }

        for(int i=0;i<=3;i++)
            for(int j=0;j<=1;j++)
                Cnt[i][j]=0;
    }

    private void readFile() {
        BufferedReader reader = null;
        try {
            ip = 0;
            ins = new Instruction[MaxIns];
            icnt = 0;
            reader = new BufferedReader(new FileReader(file));
            String temp = null;

            while((temp = reader.readLine()) != null) {
                String[] items = temp.split(" ");
                ins[icnt] = new Instruction(Integer.parseInt(items[0].trim()), items[1].trim());
                icnt++;
            }
            reader.close();
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void simExecStep(boolean Simall) {
        ip %= icnt;
        if (ip == 0) {
            initCache();
        }
        int op = ins[ip].op;
        int index = ins[ip].index;
        int tag = ins[ip].tag;
        int inblockAddr = ins[ip].inblockAddr;
        boolean Hitflag = false;

        if (cacheType == 0) {
            if (op == 2) {
                Hitflag = uCache.read(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[1][0]++;
                }
                else {
                    Cnt[1][1]++;
                    uCache.replace(tag, index);
                }
            }

            else if (op == 0) {
                Hitflag = uCache.read(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[2][0]++;
                }
                else {
                    Cnt[2][1]++;
                    uCache.replace(tag, index);
                }
            }

            else if (op == 1) {
                Hitflag = uCache.write(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[3][0]++;
                }
                else {
                    Cnt[3][1]++;
                    if (allocIndex == 0) {
                        uCache.replace(tag, index);
                        uCache.write(tag, index, inblockAddr);
                    }
                }
            }
        }
        else if (cacheType == 1) {
            if (op == 2) {
                Hitflag = iCache.read(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[1][0]++;
                } else {
                    Cnt[1][1]++;
                    iCache.replace(tag, index);
                }
            }

            else if (op == 0) {
                Hitflag = dCache.read(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[2][0]++;
                } else {
                    Cnt[2][1]++;
                    dCache.replace(tag, index);
                }
            }

            else if (op == 1) {
                Hitflag = dCache.write(tag, index, inblockAddr);
                if (Hitflag) {
                    Cnt[3][0]++;
                } else {
                    Cnt[3][1]++;
                    if (allocIndex == 0) {
                        dCache.replace(tag, index);
                        dCache.write(tag, index, inblockAddr);
                    }
                }
            }
        }

        if ((!Simall) || ip == icnt - 1)
            UIUpdate(ins[ip], Hitflag);
        ip++;
    }

    private void UIUpdate(Instruction inst, boolean Hitflag) {
        Cnt[0][1] = Cnt[1][1] + Cnt[2][1] + Cnt[3][1];
        Cnt[0][0] = Cnt[1][0] + Cnt[2][0] + Cnt[3][0];

        for(int i=0;i<=3;i++)
        {
            resultDataLabel[i][0].setText(Cnt[i][0]+Cnt[i][1] + "");
            resultDataLabel[i][1].setText(Cnt[i][1] + "");
            resultDataLabel[i][2].setText(String.format("%.2f", (((double)Cnt[i][1] / (double)(Cnt[i][1]+Cnt[i][0])) * 100)) + "%");
        }

        accessTypeDataLabel.setText("啥玩意啊.jpg");
        for(int i=0;i<=2;i++)
            if (inst.op == i)
                accessTypeDataLabel.setText(typename[i]);

        addressDataLabel.setText(inst.addr);
        blockNumberDataLabel.setText(inst.blockAddr + "");
        indexDataLabel.setText(inst.index + "");
        inblockAddressDataLabel.setText(inst.inblockAddr + "");

        int j = Hitflag?1:0;
        hitDataLabel.setText(hitname[j]);
    }

    private void simExecAll() {
        while (ip < icnt)
            simExecStep(true);
    }

    private int log2(int x) {
        return (int)(Math.log(x) / Math.log(2));
    }

    private void unifiedCacheEnabled(boolean enabled) {
        unifiedCacheButton.setSelected(enabled);
        csLabel.setEnabled(enabled);
        csBox.setEnabled(enabled);
    }

    private void separateCacheEnabled(boolean enabled) {
        separateCacheButton.setSelected(enabled);
        icsLabel.setEnabled(enabled);
        dcsLabel.setEnabled(enabled);
        icsBox.setEnabled(enabled);
        dcsBox.setEnabled(enabled);
    }

    private void draw() {
        setLayout(new BorderLayout(5,5));
        panelTop = new JPanel();
        panelLeft = new JPanel();
        panelRight = new JPanel();
        panelBottom = new JPanel();
        panelTop.setPreferredSize(new Dimension(800, 50));
        panelLeft.setPreferredSize(new Dimension(300, 600));
        panelRight.setPreferredSize(new Dimension(500, 600));
        panelBottom.setPreferredSize(new Dimension(800, 100));
        panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        labelTop = new JLabel("<html>Cache Simulator<br>Created by 陈旻宇<html>");
        labelTop.setAlignmentX(CENTER_ALIGNMENT);
        panelTop.add(labelTop);

        labelLeft = new JLabel("Cache 参数设置");
        labelLeft.setPreferredSize(new Dimension(300, 40));

        csLabel = new JLabel("总大小");
        csLabel.setPreferredSize(new Dimension(80, 30));
        csBox = new JComboBox(cachesize);
        csBox.setPreferredSize(new Dimension(90, 30));
        csBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                csIndex = csBox.getSelectedIndex();
            }
        });

        unifiedCacheButton = new JRadioButton("统一Cache:", true);
        unifiedCacheButton.setPreferredSize(new Dimension(100, 30));
        unifiedCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                separateCacheEnabled(false);
                unifiedCacheEnabled(true);
                cacheType = 0;
            }
        });

        separateCacheButton = new JRadioButton("独立Cache:");
        separateCacheButton.setPreferredSize(new Dimension(100, 30));
        separateCacheButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                separateCacheEnabled(true);
                unifiedCacheEnabled(false);
                cacheType = 1;
            }
        });

        icsLabel = new JLabel("指令Cache");
        icsLabel.setPreferredSize(new Dimension(80, 30));

        dcsLabel = new JLabel("数据Cache");
        dcsLabel.setPreferredSize(new Dimension(80, 30));

        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(100, 30));

        icsBox = new JComboBox(scachesize);
        icsBox.setPreferredSize(new Dimension(90, 30));
        icsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                icsIndex = icsBox.getSelectedIndex();
            }
        });

        dcsBox = new JComboBox(scachesize);
        dcsBox.setPreferredSize(new Dimension(90, 30));
        dcsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                dcsIndex = dcsBox.getSelectedIndex();
            }
        });

        separateCacheEnabled(false);
        unifiedCacheEnabled(true);

        //cache 块大小设置
        bsLabel = new JLabel("块大小");
        bsLabel.setPreferredSize(new Dimension(120, 30));
        bsBox = new JComboBox(blocksize);
        bsBox.setPreferredSize(new Dimension(160, 30));
        bsBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                bsIndex = bsBox.getSelectedIndex();
            }
        });

        //相连度设置
        wayLabel = new JLabel("相联度");
        wayLabel.setPreferredSize(new Dimension(120, 30));
        wayBox = new JComboBox(way);
        wayBox.setPreferredSize(new Dimension(160, 30));
        wayBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                wayIndex = wayBox.getSelectedIndex();
            }
        });

        //替换策略设置
        replaceLabel = new JLabel("替换策略");
        replaceLabel.setPreferredSize(new Dimension(120, 30));
        replaceBox = new JComboBox(replace);
        replaceBox.setPreferredSize(new Dimension(160, 30));
        replaceBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                replaceIndex = replaceBox.getSelectedIndex();
            }
        });

        //欲取策略设置
        prefetchLabel = new JLabel("预取策略");
        prefetchLabel.setPreferredSize(new Dimension(120, 30));
        prefetchBox = new JComboBox(pref);
        prefetchBox.setPreferredSize(new Dimension(160, 30));
        prefetchBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                prefetchIndex = prefetchBox.getSelectedIndex();
            }
        });
        prefetchBox.setEnabled(false);

        //写策略设置
        writeLabel = new JLabel("写策略");
        writeLabel.setPreferredSize(new Dimension(120, 30));
        writeBox = new JComboBox(write);
        writeBox.setPreferredSize(new Dimension(160, 30));
        writeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                writeIndex = writeBox.getSelectedIndex();
            }
        });

        //调块策略
        allocLabel = new JLabel("写不命中的调块策略");
        allocLabel.setPreferredSize(new Dimension(120, 30));
        allocBox = new JComboBox(alloc);
        allocBox.setPreferredSize(new Dimension(160, 30));
        allocBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                allocIndex = allocBox.getSelectedIndex();
            }
        });

        //选择指令流文件
        fileLabel = new JLabel("选择文件");
        fileLabel.setPreferredSize(new Dimension(120, 30));
        fileAddrBtn = new JLabel();
        fileAddrBtn.setPreferredSize(new Dimension(210,30));
        fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        fileBotton = new JButton("浏览...");
        fileBotton.setPreferredSize(new Dimension(70,30));
        fileBotton.addActionListener(this);

        panelLeft.add(labelLeft);
        panelLeft.add(unifiedCacheButton);
        panelLeft.add(csLabel);
        panelLeft.add(csBox);
        panelLeft.add(separateCacheButton);
        panelLeft.add(icsLabel);
        panelLeft.add(icsBox);
        panelLeft.add(emptyLabel);
        panelLeft.add(dcsLabel);
        panelLeft.add(dcsBox);
        panelLeft.add(bsLabel);
        panelLeft.add(bsBox);
        panelLeft.add(wayLabel);
        panelLeft.add(wayBox);
        panelLeft.add(replaceLabel);
        panelLeft.add(replaceBox);
        panelLeft.add(prefetchLabel);
        panelLeft.add(prefetchBox);
        panelLeft.add(writeLabel);
        panelLeft.add(writeBox);
        panelLeft.add(allocLabel);
        panelLeft.add(allocBox);
        panelLeft.add(fileLabel);
        panelLeft.add(fileAddrBtn);
        panelLeft.add(fileBotton);

        //*****************************右侧面板绘制*****************************************//
        rightLabel = new JLabel("模拟结果",JLabel.CENTER);
        rightLabel.setPreferredSize(new Dimension(500, 40));
        panelRight.add(rightLabel);

        resultTagLabel = new JLabel[4][3];
        resultDataLabel = new JLabel[4][3];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                resultTagLabel[i][j] = new JLabel(PrintTagLabel[i][j]);
                resultTagLabel[i][j].setPreferredSize(new Dimension(70, 40));
                resultDataLabel[i][j] = new JLabel("0");

                resultDataLabel[i][j].setPreferredSize(new Dimension(80, 40));

                panelRight.add(resultTagLabel[i][j]);
                panelRight.add(resultDataLabel[i][j]);
            }
            if (i == 0) {
                includelabel = new JLabel("其中",JLabel.CENTER);
                includelabel.setPreferredSize(new Dimension(500, 40));
                panelRight.add(includelabel);
            }
        }

        accessTypeTagLabel = new JLabel("访问类型:", JLabel.LEFT);
        addressTagLabel = new JLabel("地址:");
        blockNumberTagLabel = new JLabel("块号:");
        inblockAddressTagLabel = new JLabel("块内地址:");
        indexTagLabel = new JLabel("索引:");
        hitTagLabel = new JLabel("命中情况:");

        accessTypeDataLabel = new JLabel(" ");
        addressDataLabel = new JLabel(" ");
        blockNumberDataLabel = new JLabel(" ");
        indexDataLabel = new JLabel(" ");
        inblockAddressDataLabel = new JLabel(" ");
        hitDataLabel = new JLabel(" ");

        accessTypeTagLabel.setPreferredSize(new Dimension(60, 40));
        accessTypeDataLabel.setPreferredSize(new Dimension(100, 40));
        addressTagLabel.setPreferredSize(new Dimension(150, 40));
        addressDataLabel.setPreferredSize(new Dimension(150, 40));
        blockNumberTagLabel.setPreferredSize(new Dimension(60, 40));
        blockNumberDataLabel.setPreferredSize(new Dimension(100, 40));
        inblockAddressTagLabel.setPreferredSize(new Dimension(150, 40));
        inblockAddressDataLabel.setPreferredSize(new Dimension(150, 40));
        indexTagLabel.setPreferredSize(new Dimension(60, 40));
        indexDataLabel.setPreferredSize(new Dimension(100, 40));
        hitTagLabel.setPreferredSize(new Dimension(150, 40));
        hitDataLabel.setPreferredSize(new Dimension(150, 40));

        panelRight.add(accessTypeTagLabel);
        panelRight.add(accessTypeDataLabel);
        panelRight.add(addressTagLabel);
        panelRight.add(addressDataLabel);
        panelRight.add(blockNumberTagLabel);
        panelRight.add(blockNumberDataLabel);
        panelRight.add(inblockAddressTagLabel);
        panelRight.add(inblockAddressDataLabel);
        panelRight.add(indexTagLabel);
        panelRight.add(indexDataLabel);
        panelRight.add(hitTagLabel);
        panelRight.add(hitDataLabel);

        bottomLabel = new JLabel("执行控制",JLabel.CENTER);
        bottomLabel.setPreferredSize(new Dimension(800, 30));
        execStepBtn = new JButton("单步执行");
        execStepBtn.setLocation(300, 30);
        execStepBtn.addActionListener(this);
        execAllBtn = new JButton("全部执行");
        execAllBtn.setLocation(300, 30);
        execAllBtn.addActionListener(this);

        panelBottom.add(bottomLabel);
        panelBottom.add(execStepBtn);
        panelBottom.add(execAllBtn);

        add("North", panelTop);
        add("West", panelLeft);
        add("Center", panelRight);
        add("South", panelBottom);
        setSize(800, 650);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new CacheSim();
    }
}
