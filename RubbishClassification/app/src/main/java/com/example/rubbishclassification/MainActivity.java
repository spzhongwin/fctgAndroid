package com.example.rubbishclassification;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.rubbishclassification.fragment.MineFragment;
import com.example.rubbishclassification.fragment.WorkFragment;

public class MainActivity extends AppCompatActivity {

    //创建fragment变量
    private MineFragment mineFragment;
    private WorkFragment workFragment;
    //当前容器中的fragment
    private Fragment fragment_now = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //设置默认项目
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            changePageFragment(item.getItemId());
            return true;
        }
    };

    public void changePageFragment(int id) {
        switch (id) {
            case R.id.navigation_home:
                if (workFragment == null){
                    workFragment = workFragment.newInstance();
                }
                switchFragment(fragment_now, workFragment);
                break;
            case R.id.navigation_dashboard:
                if (mineFragment == null){
                    mineFragment = mineFragment.newInstance();
                }
                switchFragment(fragment_now, mineFragment);
                break;
        }
    }

    public void switchFragment(Fragment from, Fragment to) {
        if (to == null)
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!to.isAdded()) {
            if (from == null) {
                transaction.add(R.id.frame_layout, to).show(to).commit();
            } else {
                transaction.hide(from).add(R.id.frame_layout, to).commitAllowingStateLoss();
            }
        } else {
            transaction.hide(from).show(to).commit();
        }
        fragment_now = to;
    }

    // 物理返回键退出程序
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime =  System.currentTimeMillis();
            }else{
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
