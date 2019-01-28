package com.rowsen.serial_bluetoothusb;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import androidx.appcompat.app.AppCompatActivity;

public class SetActivity extends AppCompatActivity {
    ExpandableListView expand;
    BaseExpandableListAdapter adp;
    Button confirm;
    String[] title = {"波特率", "数据位", "校验位", "停止位"};
    String[][] setItem = {{"2400", "4800", "9600", "38400", "74880", "115200"}, {"7", "8"}, {"无校验(N)", "奇校验(ODD)", "偶校验(EVEN)"}, {"1", "2"}};
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        expand = findViewById(R.id.expand);
        confirm = findViewById(R.id.confirm);
        MyApp.sp = getSharedPreferences("SerialPortSet", MODE_PRIVATE);
        final SharedPreferences.Editor ed = MyApp.sp.edit();
        adp = new BaseExpandableListAdapter() {

            @Override
            public int getGroupCount() {
                return title.length;
            }

            @Override
            public int getChildrenCount(int i) {
                return setItem[i].length;
            }

            @Override
            public Object getGroup(int i) {
                return i;
            }

            @Override
            public Object getChild(int i, int i1) {
                return null;
            }

            @Override
            public long getGroupId(int i) {
                return i;
            }

            @Override
            public long getChildId(int i, int i1) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
                View v;
                if (view == null)
                    v = View.inflate(getApplication(), R.layout.title_set, null);
                else v = view;
                TextView set_title = v.findViewById(R.id.title);
                String temp = MyApp.sp.getString(title[i], null);
                if (temp == null)
                    set_title.setText(title[i]);
                else {
                    title[i] += ":" + temp;
                    set_title.setText(title[i]);
                }
                set_title.setTextSize(36);
                set_title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                return v;
            }

            @Override
            public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
                View v;
                if (view == null)
                    v = View.inflate(getApplication(), R.layout.title_set, null);
                else v = view;
                TextView item = v.findViewById(R.id.title);
                item.setText(setItem[i][i1]);
                item.setTextColor(getResources().getColor(R.color.white));
                item.setTextSize(28);
                return v;
            }

            @Override
            public boolean isChildSelectable(int i, int i1) {
                return true;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public void onGroupExpanded(int i) {

            }

            @Override
            public void onGroupCollapsed(int i) {

            }

            @Override
            public long getCombinedChildId(long l, long l1) {
                return 0;
            }

            @Override
            public long getCombinedGroupId(long l) {
                return 0;
            }

        };
        expand.setAdapter(adp);
        expand.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                TextView tx = view.findViewById(R.id.title);
                title[i] = title[i].split(":")[0] + ":" + tx.getText();
                expandableListView.collapseGroup(i);
                adp.notifyDataSetChanged();
                return false;
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int n = 0;
                for (; n < title.length; ) {
                    String[] sets = title[n].split(":");
                    if (sets.length == 2) {
                        ed.putString(sets[0].trim(), sets[1].trim());
                        if (ed.commit())
                            System.out.println("数据提交成功！" + MyApp.sp.getString(sets[0], null));
                        else {
                            System.out.println("提交数据失败！");
                            Toast.makeText(SetActivity.this, "数据提交失败！", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        n++;
                    } else if (sets.length == 1) {
                        flag = false;
                        Toast.makeText(getApplication(), "你的配置不完整！", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (n == 4) flag = true;
                System.out.println(MyApp.sp.getAll());
                //检验位的表达现在是中文，必须转为可转为整型的字符串再保存或者再存一组整型的数据，这里为了显示统一采用第二种策略
                String party_bit = MyApp.sp.getString("校验位", "无校验(N)");
                switch (party_bit) {
                    case "无校验(N)":
                        ed.putInt("PARITY", UsbSerialPort.PARITY_NONE);
                        ed.commit();
                        break;
                    case "奇校验(ODD)":
                        ed.putInt("PARITY", UsbSerialPort.PARITY_ODD);
                        ed.commit();
                        break;
                    case "偶校验(EVEN)":
                        ed.putInt("PARITY", UsbSerialPort.PARITY_EVEN);
                        ed.commit();
                        break;
                }
                System.out.println(MyApp.sp.getAll());
                if (flag) finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        confirm.performClick();
    }
}
