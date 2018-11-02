package belaku.np.i.bluee;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH = 1;
    private static final int MY_PERMISSIONS_REQUEST = 2;
    private static final int REQUEST_PAIR_DEVICE = 2;
    private static final UUID MY_UUID = UUID.fromString( "0000110A-0000-1000-8000-00805F9B34FB" );
    private BluetoothAdapter BTAdapter;
    private TextView TxBtDetails;
    private Switch SwitchEnableDisable;
    private Button BtnPairedDevices, BtnNearbyDevices;
    public ListView NearbyLvDevices, PairedLvDevices;
    public ArrayList<String> DeviceNames = new ArrayList<>();
    private ArrayList<String> nullNames = new ArrayList<>();
    private ArrayAdapter<String> nullAdapter;
    private TextView TxListName;
    private ArrayList<BluetoothDevice> pairedDeviceList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nullAdapter  = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, nullNames);
        CheckPermissions();

        TxBtDetails = findViewById(R.id.tx_bluetooth_details);
        SwitchEnableDisable = findViewById(R.id.switch_enabled_disable);
        BtnPairedDevices = findViewById(R.id.btn_paired_devices);
        BtnNearbyDevices = findViewById(R.id.btn_nearby_devices);
        NearbyLvDevices = findViewById(R.id.nearby_device_list);
        PairedLvDevices = findViewById(R.id.paired_device_list);
        TxListName = findViewById(R.id.list_name);


        BtnNearbyDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFilter filter = new IntentFilter();

                ensureDiscoverable();

                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


                DeviceNames.clear();
            //    DeviceNames.add("Nearby Devices - ");
                registerReceiver(mReceiver, filter);
                BTAdapter.startDiscovery();

            }
        });
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            makeSnack("Device supports Bluetooth");
            TxBtDetails.setText(BTAdapter.getName());
            if (!BTAdapter.isEnabled()) {
                TxBtDetails.append(" \n And is disabled");
                SwitchEnableDisable.setChecked(false);
                BtnPairedDevices.setEnabled(false);
                BtnNearbyDevices.setEnabled(false);
            } else {
                TxBtDetails.append(" \n And is enabled");
                BtnPairedDevices.setEnabled(true);
                BtnNearbyDevices.setEnabled(true);
                SwitchEnableDisable.setChecked(true);
            }


        }

        SwitchEnableDisable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_BLUETOOTH);
                } else {
                    BTAdapter.disable();
                    TxBtDetails.append("\n DISABLED now");
                }

            }
        });

        BtnPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                TxListName.setText("Paired Devices -");
                nullAdapter  = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, nullNames);
                PairedLvDevices.setAdapter(nullAdapter);

                DeviceNames.clear();

                Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
                pairedDeviceList = new ArrayList(pairedDevices);

                if (pairedDeviceList.size() == 0) {
                    makeSnack("No Paired Devices");
                    TxBtDetails.append(" \n No Paired Devices");
                }
                else {
                    for (int i = 0; i < pairedDeviceList.size(); i++)
                        DeviceNames.add(pairedDeviceList.get(i).getName());
                }
                ArrayAdapter<String> arrayAdapter =
                        new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, DeviceNames);


                // Set The Adapter
                NearbyLvDevices.setVisibility(View.INVISIBLE);
                PairedLvDevices.setVisibility(View.VISIBLE);
                PairedLvDevices.setAdapter(arrayAdapter);
            }
        });


    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    private void ensureDiscoverable() {

        if (BTAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void CheckPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private ArrayList<BluetoothDevice> NearByBtDevices = new ArrayList<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                makeSnack("Searching for nearby BT devices..");
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                makeSnack("Ending the Search!");
                ArrayAdapter<String> arrayAdapter =
                        new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, DeviceNames);
                TxListName.setText("NearBy Devices -");
                NearbyLvDevices.setVisibility(View.VISIBLE);
                PairedLvDevices.setVisibility(View.INVISIBLE);
                nullAdapter  = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, nullNames);
                NearbyLvDevices.setAdapter(nullAdapter);
                NearbyLvDevices.setAdapter(arrayAdapter);


                PairedLvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        makeSnack(pairedDeviceList.get(i).getName());
                        BTAdapter.cancelDiscovery();
                      //  Connect(pairedDeviceList.get(i));
                       makeSnack("Yet2Impl connectivity");
                    }
                });

                NearbyLvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    makeSnack(NearByBtDevices.get(i).getName());
                        try {
                            if (createBond(NearByBtDevices.get(i)))
                                makeSnack("Paired with " + NearByBtDevices.get(i).getName());
                        } catch (Exception e) {
                            makeSnack(e.toString());
                        }
                    }
                });

                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    makeSnack("Found device " + device.getName());
                    NearByBtDevices.add(device);
                    DeviceNames.add(device.getName());
                }
            }
        }
    };

    private void Connect(BluetoothDevice device) {

        MainActivity mainActivity = new MainActivity();
        String TAG = "CONNECT";
        BluetoothSocket mSocket = null;
        UUID MY_UUID = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");

        if (device.getBondState() == device.BOND_BONDED) {
            Log.d(TAG, device.getName());
            try {
                mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                Log.d(TAG, "socket not created");
                mainActivity.makeSnack("socket not created");
                e1.printStackTrace();
            }

            try {
                mSocket.connect();
                mainActivity.makeSnack("CONNECTED !" + device.getName());
            } catch (IOException e) {
                try {

                    mSocket.close();
            //        mainActivity.makeSnack("Cannot connect");
                    Log.d(TAG, "Cannot connect");
                } catch (IOException e1) {
                    Log.d(TAG, "Socket not closed");
                    mainActivity.makeSnack("Socket not closed");
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                BtnPairedDevices.setEnabled(true);
                BtnNearbyDevices.setEnabled(true);
                TxBtDetails.append("\n ENABLED now !");
                SwitchEnableDisable.setChecked(true);
            } else {
                BtnPairedDevices.setEnabled(false);
                BtnNearbyDevices.setEnabled(false);
                TxBtDetails.append("User denied to enable BT");
                SwitchEnableDisable.setChecked(false);
            }
        }
    }

    private void makeSnack(String s) {
        Snackbar.make(getWindow().getDecorView().getRootView(), s + "\n \n \n ", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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



}
