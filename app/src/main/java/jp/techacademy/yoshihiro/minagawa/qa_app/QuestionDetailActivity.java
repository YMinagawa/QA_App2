package jp.techacademy.yoshihiro.minagawa.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


//OnCreate内ではQuestionクラスのインスタンスを保持し、タイトルを設定します。
//そして、ListViewを用意する
//FABをタップしたら、ログインしていなければログイン画面に遷移させ、ログインしていれば
//後ほど作成する回答作成画面に遷移させる準備をしておく。
//重要なのはFirebaseへのリスナーの登録。回答作成画面から戻ってきた時にその回答を表示させるために登録

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            HashMap map = (HashMap)dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()){
                //同じAnswerUidのものが存在しているときは何もしない
                if(answerUid.equals(answer.getAnswerUid())){
                    return;
                }
            }

            String body = (String)map.get("body");
            String name = (String)map.get("name");
            String uid = (String)map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        //渡ってきたQuestionオブジェクトを保存する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question)extras.get("question");

        setTitle(mQuestion.getTitle());

        //ListViewの準備
        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user == null){
                    //ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    //Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = databaseReference.child(Const.ContentPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuesitionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }
}