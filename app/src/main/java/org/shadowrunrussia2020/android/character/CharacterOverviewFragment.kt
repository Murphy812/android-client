package org.shadowrunrussia2020.android.character


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_character_overview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shadowrunrussia2020.android.R
import org.shadowrunrussia2020.android.qr.showErrorMessage

class CharacterOverviewFragment : Fragment() {

    private lateinit var mModel: CharacterViewModel
    private val database = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_character_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mModel = ViewModelProviders.of(activity!!).get(CharacterViewModel::class.java)
        mModel.getCharacter().observe(this, Observer {
            if (it != null) {
                textViewMaxHp.text = it.maxHp.toString()
                textViewMagic.text = it.magic.toString()
                textViewMagicPowerBonus.text = it.magicPowerBonus.toString()
            }
        })

        database.getReference("spellsCasted").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val p = dataSnapshot.getValue(Int::class.java) ?: return
                textTest1.text = p.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        })

        buttonTest1.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.getReference("spellsCasted").runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val p = mutableData.getValue(Int::class.java)
                            ?: return Transaction.success(mutableData)
                        mutableData.value = p + 1
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {}
                })
            }
        }

        val docRef = firestore.collection("characters").document("8")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("Firestore", "Current data: ${snapshot.data}")
                textTest2.text = snapshot.data!!["spellsCasted"].toString()
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }

        buttonTest2.setOnClickListener {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val newPopulation = snapshot.getLong("spellsCasted")!! + 1
                transaction.update(docRef, "spellsCasted", newPopulation)
            }
        }

        swipeRefreshLayout.setOnRefreshListener { refreshData() }
    }

    private fun refreshData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) { mModel.refresh() }
            } catch (e: Exception) {
                showErrorMessage(requireContext(), "Ошибка. ${e.message}")
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }
}
