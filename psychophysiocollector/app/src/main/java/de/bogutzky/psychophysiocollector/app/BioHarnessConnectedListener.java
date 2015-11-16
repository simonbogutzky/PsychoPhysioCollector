package de.bogutzky.psychophysiocollector.app;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import zephyr.android.BioHarnessBT.*;

public class BioHarnessConnectedListener extends ConnectListenerImpl {
    private Handler _aNewHandler;
    final int GP_MSG_ID = 0x20;
    final int BREATHING_MSG_ID = 0x21;
    final int ECG_MSG_ID = 0x22;
    final int RtoR_MSG_ID = 0x24;
    final int ACCEL_100mg_MSG_ID = 0x2A;
    final int SUMMARY_MSG_ID = 0x2B;
    final int RR_INTERVAL = 0x105;

    private final int HEART_RATE = 0x100;
    private final int RESPIRATION_RATE = 0x101;
    private final int SKIN_TEMPERATURE = 0x102;
    private final int POSTURE = 0x103;
    private final int PEAK_ACCLERATION = 0x104;
    /*Creating the different Objects for different types of Packets*/
    private GeneralPacketInfo GPInfo = new GeneralPacketInfo();
    private ECGPacketInfo ECGInfoPacket = new ECGPacketInfo();
    private BreathingPacketInfo BreathingInfoPacket = new BreathingPacketInfo();
    private RtoRPacketInfo RtoRInfoPacket = new RtoRPacketInfo();
    private AccelerometerPacketInfo AccInfoPacket = new AccelerometerPacketInfo();
    private SummaryPacketInfo SummaryInfoPacket = new SummaryPacketInfo();

    private PacketTypeRequest RqPacketType = new PacketTypeRequest();

    public BioHarnessConnectedListener(Handler handler, Handler _NewHandler) {
        super(handler, null);
        _aNewHandler = _NewHandler;
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
        Message msg = new Message();
        msg.what = 101; //ready msg
        _aNewHandler.sendMessage(msg);
        /*Use this object to enable or disable the different Packet types*/
        RqPacketType.GP_ENABLE = true;
        RqPacketType.RtoR_ENABLE = true;
        RqPacketType.ECG_ENABLE = false;
        RqPacketType.ACCELEROMETER_ENABLE = false;
        RqPacketType.BREATHING_ENABLE = false;
        RqPacketType.LOGGING_ENABLE = true;
        RqPacketType.SUMMARY_ENABLE = true;


        //Creates a new ZephyrProtocol object and passes it the BTComms object
        ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), RqPacketType);
        //ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
        _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                ZephyrPacketArgs msg = eventArgs.getPacket();
                int MsgID = msg.getMsgID();
                byte[] DataArray = msg.getBytes();
                switch (MsgID) {

                    case GP_MSG_ID:

                        //***************Displaying the Heart Rate********************************
                        int HRate = GPInfo.GetHeartRate(DataArray);
                        Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
                        Bundle b1 = new Bundle();
                        b1.putString("HeartRate", String.valueOf(HRate));
                        b1.putLong("Timestamp", System.currentTimeMillis());
                        text1.setData(b1);
                        _aNewHandler.sendMessage(text1);
                        //System.out.println("Heart Rate is "+ HRate);

                        //***************Displaying the Respiration Rate********************************
                        double RespRate = GPInfo.GetRespirationRate(DataArray);

                        text1 = _aNewHandler.obtainMessage(RESPIRATION_RATE);
                        b1.putString("RespirationRate", String.valueOf(RespRate));
                        b1.putLong("Timestamp", System.currentTimeMillis());
                        text1.setData(b1);
                        _aNewHandler.sendMessage(text1);
                        //System.out.println("Respiration Rate is "+ RespRate);

                        //***************Displaying the Skin Temperature*******************************


                        double SkinTempDbl = GPInfo.GetSkinTemperature(DataArray);
                        text1 = _aNewHandler.obtainMessage(SKIN_TEMPERATURE);
                        //Bundle b1 = new Bundle();
                        b1.putLong("Timestamp", System.currentTimeMillis());
                        b1.putString("SkinTemperature", String.valueOf(SkinTempDbl));
                        text1.setData(b1);
                        _aNewHandler.sendMessage(text1);
                        //System.out.println("Skin Temperature is "+ SkinTempDbl);

                        //***************Displaying the Posture******************************************

                        int PostureInt = GPInfo.GetPosture(DataArray);
                        text1 = _aNewHandler.obtainMessage(POSTURE);
                        b1.putString("Posture", String.valueOf(PostureInt));
                        b1.putLong("Timestamp", System.currentTimeMillis());
                        text1.setData(b1);
                        _aNewHandler.sendMessage(text1);
                        //System.out.println("Posture is "+ PostureInt);
                        //***************Displaying the Peak Acceleration******************************************

                        double PeakAccDbl = GPInfo.GetPeakAcceleration(DataArray);
                        text1 = _aNewHandler.obtainMessage(PEAK_ACCLERATION);
                        b1.putString("PeakAcceleration", String.valueOf(PeakAccDbl));
                        b1.putLong("Timestamp", System.currentTimeMillis());
                        text1.setData(b1);
                        _aNewHandler.sendMessage(text1);
                        //System.out.println("Peak Acceleration is "+ PeakAccDbl)

                        break;
                    case BREATHING_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                        //System.out.println("Breathing Packet Sequence Number is "+BreathingInfoPacket.GetSeqNum(DataArray));
                        break;
                    case ECG_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                        //System.out.println("ECG Packet Sequence Number is "+ECGInfoPacket.GetSeqNum(DataArray));
                        break;
                    case RtoR_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                        //System.out.println("R to R Packet Sequence Number is "+RtoRInfoPacket.GetSeqNum(DataArray));
                        int[] rrIntervals = RtoRInfoPacket.GetRtoRSamples(DataArray);
                        int time = 0;
                        int rrInterval = 0;
                        for (int i = 0; i < rrIntervals.length; i++) {
                            if (rrIntervals[i] < 2000) {
                                if (rrInterval != rrIntervals[i]) {
                                    time = time + rrIntervals[i];
                                    rrInterval = rrIntervals[i];
                                    Message rrMessage = new Message();
                                    rrMessage.what = RR_INTERVAL;
                                    Bundle rrBundle = new Bundle();
                                    rrBundle.putInt("rrinterval", rrInterval);
                                    rrBundle.putLong("Timestamp", System.currentTimeMillis());
                                    rrBundle.putInt("rrTime" , time);
                                    rrMessage.setData(rrBundle);
                                    _aNewHandler.sendMessage(rrMessage);
                                    time = 0;
                                }
                            }
                        }

                        break;
                    case ACCEL_100mg_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                        //System.out.println("Accelerometry Packet Sequence Number is "+AccInfoPacket.GetSeqNum(DataArray));
                        break;
                    case SUMMARY_MSG_ID:
					/*Do what you want. Printing Sequence Number for now*/
                        //System.out.println("Summary Packet Sequence Number is "+SummaryInfoPacket.GetSeqNum(DataArray));
                        int hrv = SummaryInfoPacket.GetHearRateVariability(DataArray);
                        break;

                }
            }
        });
    }

}