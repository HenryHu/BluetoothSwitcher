package net.henryhu.bluetoothswitcher;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private Button btWifiStatus = null;
        private TextView tStatus = null;
        private TextView tBtState = null;

        private WifiManager wifiMgr;
        private BluetoothAdapter adapter;
        private BluetoothDevice panDevice;

        private BluetoothPan panProxy;


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
//            btMgr = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = BluetoothAdapter.getDefaultAdapter();
            final int PAN_PROFILE = 5; // hidden profile
            adapter.getProfileProxy(getActivity(), new BluetoothProfile.ServiceListener() {

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    panProxy = new BluetoothPan(proxy);
                }

                @Override
                public void onServiceDisconnected(int profile) {
                    panProxy = null;
                }
            }, PAN_PROFILE);

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            tStatus = (TextView)rootView.findViewById(R.id.tState);
            tBtState = (TextView)rootView.findViewById(R.id.tBtState);
            btWifiStatus = (Button)rootView.findViewById(R.id.btWifiState);
            btWifiStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    WifiInfo info = wifiMgr.getConnectionInfo();
                    if (info.getIpAddress() != 0) {
                        tStatus.setText(info.getSSID());
                    } else {
                        tStatus.setText("not connected");
                    }
                }
            });
            rootView.findViewById(R.id.btBtState).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!adapter.isEnabled()) {
                        adapter.enable();
                        tBtState.setText("Turning on adapter\n");
                    } else {
                        tBtState.setText("Available devices:");
                        for (BluetoothDevice dev : adapter.getBondedDevices()) {
                            BluetoothClass btClass = dev.getBluetoothClass();
                            if (btClass.hasService(BluetoothClass.Service.NETWORKING)) {
                                tBtState.append("\n" + dev.getName());
                                panDevice = dev;
                            }
                        }
                    }
                }
            });
            rootView.findViewById(R.id.bConnect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (panDevice != null) {
                        panProxy.connect(panDevice);
                    } else {
                        showError("Find device first");
                    }
                }
            });
            rootView.findViewById(R.id.bDisconnect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (panDevice != null) {
                        panProxy.disconnect(panDevice);
                    } else {
                        showError("Find device first");
                    }
                }
            });
            return rootView;
        }

        void showError(String error) {

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
