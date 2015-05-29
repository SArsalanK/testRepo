package com.gazuntite.provider.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gazuntite.model.ProviderSummary;
import com.gazuntite.model.RingCall;
import com.gazuntite.model.RingCallResponseModel;
import com.gazuntite.model.UserCommunicationStatus;
import com.gazuntite.provider.R;
import com.gazuntite.provider.call.Async_saveConference;
import com.gazuntite.provider.model.CommunicationModel;
import com.gazuntite.provider.model.CommunicationRequestModel;
import com.gazuntite.provider.model.CommunicationSaveModel;
import com.gazuntite.provider.model.CommunicationSaveRequestModel;
import com.gazuntite.provider.model.ResponseModel;
import com.gazuntite.provider.model.SaveResponseModel;
import com.gazuntite.provider.service.CommunicationService;
import com.gazuntite.provider.utility.Constants;
import com.gazuntite.provider.utility.Session;
import com.oovoo.core.ClientCore.VideoChannelPtr;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.ui.VideoRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class arsalan_CommunicationCallFragment extends Fragment implements
        IConferenceCoreListener {

	// Communication Declaration Start

    private String AppID = "12349983353074";
    private String TokenID = "MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB%2Bv%2B604ytfGejGo1nAqG9WzbgEMGEa1jbr7mVA%2BG8AHs2YFzWw2DeS7YY0qiH8MqcCqojxMw74z7sSt6EQ8H1CKkuE2URyYYYQ1Ra%2BkiX%2BWsqiOddx8cwtySP0Ibh30FY%3D";
    private String baseURL = "https://api-sdk.dev.oovoo.com";
    private EditText confID;
    private TextView confName;
    private Button startbtn;
    private Button endbtn, toggle_camera, mute;
    private SurfaceView camView;
    private static ConferenceCore conf;
    private GLSurfaceView remoteView;
    private VideoRenderer mRenderer;
    //private TextView remote_participant;
    public static TextView selected_provider;
    private Button btnVideoOn, acceptButton, rejectButton;
    private ArrayList<ProviderSummary> lstProviders = new ArrayList<ProviderSummary>();
    private ArrayList<String> selectedlstProviders = new ArrayList<String>();
    private Button sendMessage;
    private static String partId = Math.random() + "";
    private ProgressDialog mWaitingDialog;
    int cameraSelection = 0;
    public static String conferenceID = "";
    private ListView commProviderslist;
    private Adapter_CommunicationProvidersStatus adaptercomm;
    public static List<UserCommunicationStatus> model;
    private RingCall ringCall;
    MediaPlayer mPlayer;
    public Boolean inCall = false;
    public Boolean isPendingCall = false;

    // Communication Declaration End

    View view;
    public static CommunicationSummaryFragment_Data_Loader Data_loader;

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        if (mPlayer != null) {
            if (mPlayer.isPlaying())
                mPlayer.stop();
        }
        super.onDestroy();

        Toast.makeText(GazuntiteMainActivity.context, "Destroyed", Toast.LENGTH_SHORT).show();
        if (conf != null) {

            conf.leaveConference(IConferenceCore.ConferenceCoreError.OK);

            conf.turnCameraOff();

            conf.turnPreviewOff();
        }

        if (mWaitingDialog != null && mWaitingDialog.isShowing())
            mWaitingDialog.hide();
    }


    public void getCameraList() {
        Vector<ConferenceCore.MediaDevice> list = conf.getMediaDeviceList(IConferenceCore.DeviceType.Camera);
    }

    Handler myHandler = new Handler();

    private void CheckPendingCall() {
        myHandler.post(checkCall);
    }
    final Runnable checkCall = new Runnable()

    {
        @SuppressLint("SimpleDateFormat")
        public void run() {

            class Async_ringCall extends
                    AsyncTask<Void, Void, RingCallResponseModel> {

                @Override
                protected RingCallResponseModel doInBackground(Void... params) {
                    // TODO Auto-generated method stub
                    return CommunicationService.ringingCall();
                }

                @Override
                protected void onPostExecute(RingCallResponseModel result) {
                    if (result != null) {
                        RingCall rc = result.getData();

                        if (rc != null
                                && !rc.getConferenceId().equals(conferenceID)) {
                            if (!rc.getOrganizerId().equals(Session.userID)) {
                                isPendingCall = true;
                                ringCall = rc;
                                acceptButton.setVisibility(View.VISIBLE);
                                rejectButton.setVisibility(View.VISIBLE);
                                startbtn.setVisibility(View.GONE);
                                endbtn.setVisibility(View.VISIBLE);
                                conferenceID = rc.getConferenceId();
                                try {
                                    mPlayer = MediaPlayer.create(getActivity(),
                                            R.raw.ringing);
                                    mPlayer.setLooping(true);
                                    mPlayer.start();

                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Boolean isOtherUserOnline = false;
                                for (int i = 0; i < model.size(); i++) {
                                    for (int j = 0; j < rc.getParticipants().length; j++) {
                                        if (rc.getParticipants()[j]
                                                .equals(model.get(i)
                                                        .getUserid())) {
                                            isOtherUserOnline = true;
                                        }
                                    }
                                }

                                if (isOtherUserOnline) {
                                    // reconnecting
                                    isPendingCall = true;
                                    ringCall = rc;
                                    acceptButton.setVisibility(View.VISIBLE);
                                    rejectButton.setVisibility(View.VISIBLE);
                                    startbtn.setVisibility(View.GONE);
                                    endbtn.setVisibility(View.VISIBLE);
                                    conferenceID = rc.getConferenceId();
                                    acceptButton.callOnClick();

                                } else {
                                    conferenceID = rc.getConferenceId();
                                    // discard the call
                                    rejectButton.callOnClick();
                                    endbtn.callOnClick();
                                }
                            }
                        }
                    }
                }

                ;

            }

            Async_ringCall ringCall = new Async_ringCall();
            ringCall.execute();

        }
    };

	public static arsalan_CommunicationCallFragment newInstance(CharSequence label) {
		System.out.println("newInstance : " +label);
		arsalan_CommunicationCallFragment f = new arsalan_CommunicationCallFragment();
		Bundle b = new Bundle();
		b.putCharSequence("label", label);
		f.setArguments(b);
		return f;
	}
    public void startCall() {
        if (conferenceID == "") {
            selectedlstProviders.clear();
            selectedlstProviders.add(Data_loader.getUser_id());
            CommunicationSaveRequestModel commModel = new CommunicationSaveRequestModel();
            CommunicationSaveModel call = new CommunicationSaveModel();
            call.setUserid(Session.userID);
            call.setAttendees(selectedlstProviders);
            commModel.setData(call);
            commModel.setUserId(Session.userID);

            class saveConf extends Async_saveConference {
                @Override
                protected void onPostExecute(SaveResponseModel result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);
                    conferenceID = result.getData();
                    if (conferenceID != null
                            && Integer.parseInt(conferenceID) > 0) {
                        conf.joinConference(result.getData(), Session.userID,
                                "");
                        ConferenceCore.instance().turnMicrophoneOn();
                    }
                }

            }
            saveConf startConf = new saveConf();
            startConf.execute(commModel);
        } else {
            conf.joinConference(conferenceID, Session.userID, "");
            ConferenceCore.instance().turnMicrophoneOn();
        }
    }

    public void AnswerCall(final Constants.CallAcceptStatusTypes type) {
        if (!conferenceID.equals("")) {

            class async_answerCall extends AsyncTask<Void, Void, ResponseModel> {

                @Override
                protected ResponseModel doInBackground(Void... arg0) {
                    // TODO Auto-generated method stub
                    return CommunicationService.answerCall(conferenceID, type);
                }

                @Override
                protected void onPostExecute(ResponseModel result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);
                    if (type == Constants.CallAcceptStatusTypes.Accept) {
                        inCall = true;
                    } else {
                        inCall = false;
                    }
                    isPendingCall = false;

                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    startbtn.setVisibility(View.VISIBLE);
                    endbtn.setVisibility(View.VISIBLE);
                    if (inCall) {
                        endbtn.setEnabled(true);
                        startbtn.setEnabled(false);
                    } else {
                        endbtn.setEnabled(false);
                        startbtn.setEnabled(true);

                    }

                }

            }
            async_answerCall answerCall = new async_answerCall();
            answerCall.execute();
        }
    }

    public void DisconnectCall(String duration) {
        if (!conferenceID.equals("")) {

            class async_hangcall extends AsyncTask<String, Void, ResponseModel> {

                @Override
                protected ResponseModel doInBackground(String... arg0) {
                    // TODO Auto-generated method stub
                    return CommunicationService.phoneHangCall(conferenceID,
                            arg0[0]);
                }

                @Override
                protected void onPostExecute(ResponseModel result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);
                    inCall = false;

                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    startbtn.setVisibility(View.VISIBLE);
                    conferenceID = "";
                }
            }
            async_hangcall hangCall = new async_hangcall();
            hangCall.execute(duration);
        }

    }

    @Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub

        view = inflater.inflate(R.layout.com_call, container, false);
        startbtn = (Button) view.findViewById(R.id.start);
        startbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startCall();
            }
        });

        acceptButton = (Button) view.findViewById(R.id.acceptCall);
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (mPlayer != null && mPlayer.isPlaying())
                    mPlayer.stop();
                startCall();
                AnswerCall(Constants.CallAcceptStatusTypes.Accept);
            }
        });
        rejectButton = (Button) view.findViewById(R.id.rejectCall);
        rejectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // startCall();
                if (mPlayer.isPlaying())
                    mPlayer.stop();

                DisconnectCall("0");
                // AnswerCall(CallAcceptStatusTypes.Reject);
            }
        });

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CheckPendingCall();
            }
        }, 0, 20000);

        acceptButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);

        endbtn = (Button) view.findViewById(R.id.end);
        endbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                conf.leaveConference(IConferenceCore.ConferenceCoreError.OK);
                conf.turnCameraOff();
                conf.turnPreviewOff();
                conf.turnVideoTransmitOff();
                startbtn.setEnabled(true);
                endbtn.setEnabled(false);
                DisconnectCall("0");
            }
        });
        toggle_camera = (Button) view.findViewById(R.id.toggle_camera);
        mute = (Button) view.findViewById(R.id.mute);
        camView = (SurfaceView) view.findViewById(R.id.surface);

        sendMessage = (Button) view.findViewById(R.id.sendMessage);
        remoteView = (android.opengl.GLSurfaceView) view
                .findViewById(R.id.remoteCam);

        endbtn.setEnabled(false);
        if (conf == null)
        {
            conf = ConferenceCore.instance(view.getContext(), this);
            conf.initSdk(AppID, TokenID, baseURL);

            btnVideoOn = (Button) view.findViewById(R.id.videoOn);
            sendMessage = (Button) view.findViewById(R.id.sendMessage);
            remoteView = (android.opengl.GLSurfaceView) view
                    .findViewById(R.id.remoteCam);

//            ProviderLogin Provider_Data = Session.loginResult.getData();
//
//            confName.setText(Provider_Data.getLastName().toString().trim()
//                    + ", " + Provider_Data.getFirstName().toString().trim());

            endbtn.setEnabled(false);

            if (conf == null) {
                conf = ConferenceCore.instance(view.getContext(), this);
                conf.initSdk(AppID, TokenID, baseURL);

            }

            try {
                conf.setPreviewSurface(camView);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            conf.turnCameraOn();
            conf.turnPreviewOn();

            ConferenceCore.instance().setCameraResolutionLevel(
                    IConferenceCore.CameraResolutionLevel.ResolutionLow);

            ConferenceCore.instance().turnMicrophoneOn();
            ConferenceCore.instance().turnSpeakerOn();

            try {
                conf.setPreviewSurface(camView);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // conf.turnCameraOn();
            // conf.turnPreviewOn();

            ConferenceCore.instance().setCameraResolutionLevel(
                    IConferenceCore.CameraResolutionLevel.ResolutionLow);

            ConferenceCore.instance().turnMicrophoneOn();
            ConferenceCore.instance().turnSpeakerOn();

            sendMessage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    String chatMessage = ((EditText) getView().findViewById(
                            R.id.chatMessage)).getText().toString();
                    conf.inCallMessage(chatMessage.getBytes());
                }
            });
            btnVideoOn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    conf.turnCameraOn();
                    conf.turnPreviewOn();

                }
            });

            toggle_camera.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    try {

                        Vector<ConferenceCore.MediaDevice> cameraList = conf
                                .getMediaDeviceList(IConferenceCore.DeviceType.Camera);

                        if (cameraSelection < cameraList.size() - 1) {
                            cameraSelection++;
                            conf.selectCamera(cameraSelection);
                        } else {
                            cameraSelection = 0;
                            conf.selectCamera(cameraSelection);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mute.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    if (mute.getText().toString().equals("Mute")) {
                        ConferenceCore.instance().turnMicrophoneOff();
                        mute.setText("Unmute");
                    } else {
                        ConferenceCore.instance().turnMicrophoneOn();
                        mute.setText("Mute");
                    }
                }
            });

            remoteView.setEGLContextClientVersion(2);

            mRenderer = new VideoRenderer(remoteView);
            remoteView.setRenderer(mRenderer);



            return view;
        }

        remoteView.setEGLContextClientVersion(2);
        mRenderer = new VideoRenderer(remoteView);
        remoteView.setRenderer(mRenderer);
        getCameraList();
		return view;
	}

	//FOR LAYOUT ANIMATIONS
	//Below are the methods to set view from VISIBLE to GONE

	// To animate view slide out from left to right
	public void slideToRight(View view){
		TranslateAnimation animate = new TranslateAnimation(0,view.getWidth(),0,0);
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.GONE);
		view.clearAnimation();
	}
	
	// To animate view slide out from right to left
	public void slideToLeft(View view){
		TranslateAnimation animate = new TranslateAnimation(0,-view.getWidth(),0,0);
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.GONE);
		view.clearAnimation();
	}

	// To animate view slide out from top to bottom
	public void slideToBottom(View view){
		view.clearAnimation();
		TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.GONE);
		view.clearAnimation();
	}

	// To animate view slide out from bottom to top
	public void slideToTop(View view){
		TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.GONE);
		view.clearAnimation();
	}

	// To animate view slide in from bottom to top
	public void slideFromBottom(View view){
		TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.VISIBLE);
	}

	// To animate view slide in from top to bottom
	public void slideFromTop(View view){
		TranslateAnimation animate = new TranslateAnimation(0,0,-view.getHeight(),0);
		animate.setDuration(150);
		animate.setFillAfter(true);
		view.startAnimation(animate);
		view.setVisibility(View.VISIBLE);
	}

    @Override
    public void OnCameraSelected(IConferenceCore.ConferenceCoreError arg0, String arg1,
                                 String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnCameraTurnedOff(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnCameraTurnedOn(IConferenceCore.ConferenceCoreError arg0, ConferenceCore.FrameSize arg1,
                                 int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnConferenceError(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnConnectionStatisticsUpdate(IConferenceCore.ConnectionStatistics arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnGetMediaDeviceList(ConferenceCore.MediaDevice[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnHold(String arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void OnIncallMessage(byte[] arg0, String arg1) {
        // TODO Auto-generated method stub
        final byte[] ar = arg0;
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), new String(ar), Toast.LENGTH_LONG).show();
                }

            });
        }
    }



    @Override
    public void OnJoinConference(IConferenceCore.ConferenceCoreError arg0, String arg1) {
        // TODO Auto-generated method stub

        Log.e("CONNECTED", "  device");

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    endbtn.setEnabled(true);
                    startbtn.setEnabled(false);
                }

            });
        }
    }

    @Override
    public void OnLeftConference(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //remote_participant.setVisibility(View.GONE);

                    endbtn.setEnabled(false);
                    startbtn.setEnabled(true);
                }

            });
        }

        CommunicationRequestModel m = new CommunicationRequestModel();
        CommunicationModel cm = new CommunicationModel();
        cm.setConferenceId(conferenceID);
        m.setData(cm);
        m.setUserId(Session.userID);
        DisconnectCall("0");
    }

    @Override
    public void OnMicrophoneSelected(IConferenceCore.ConferenceCoreError arg0, String arg1,
                                     String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnMicrophoneTurnedOff(IConferenceCore.ConferenceCoreError arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnMicrophoneTurnedOn(IConferenceCore.ConferenceCoreError arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void OnParticipantJoinedConference(
            String sParticipantId, String sOpaqueString) {
        // TODO Auto-generated method stub

        conf.receiveParticipantVideoOn(sParticipantId);
        final String P_name;
        try {
            P_name = sParticipantId;
            conf.turnVideoTransmitOn();
            ConferenceCore.instance().turnMicrophoneOn();
            ConferenceCore.instance().turnSpeakerOn();

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        remote_participant.setVisibility(View.VISIBLE);
//                        remote_participant.setText("" + P_name);
                        if (mWaitingDialog != null)
                            mWaitingDialog.hide();

                        startbtn.setEnabled(false);
                        endbtn.setEnabled(true);
                    }

                });
            }
            Log.e("Participant id", sParticipantId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void OnParticipantLeftConference(String arg0) {
        // TODO Auto-generated method stub
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                 //   remote_participant.setVisibility(View.GONE);
                    endbtn.setEnabled(false);
                    startbtn.setEnabled(true);
                    DisconnectCall("0");
                }
            });
        }
    }

    @Override
    public void OnParticipantVideoPaused(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnParticipantVideoReceiveOff(IConferenceCore.ConferenceCoreError arg0,
                                             String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnParticipantVideoReceiveOn(IConferenceCore.ConferenceCoreError errorCode,
                                            String sParticipantId, ConferenceCore.FrameSize frameSize) {
        // TODO Auto-generated method stub

        VideoChannelPtr in = conf.getVideoChannelForUser(sParticipantId);
        mRenderer.connect(in, sParticipantId);

    }

    @Override
    public void OnParticipantVideoResumed(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnPreviewTurnedOff(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnPreviewTurnedOn(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnSpeakerSelected(IConferenceCore.ConferenceCoreError arg0, String arg1,
                                  String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnSpeakerTurnedOff(IConferenceCore.ConferenceCoreError arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnSpeakerTurnedOn(IConferenceCore.ConferenceCoreError arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnUnHold(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnVideoTransmitTurnedOff(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnVideoTransmitTurnedOn(IConferenceCore.ConferenceCoreError arg0) {
        // TODO Auto-generated method stub

    }

}
