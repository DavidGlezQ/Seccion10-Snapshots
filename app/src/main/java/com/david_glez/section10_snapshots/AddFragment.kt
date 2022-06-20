package com.david_glez.section10_snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.david_glez.section10_snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddFragment : Fragment() {

    private var RC_GALLERY = 18
    private var PATH_SNAPSHOT = "snapshots"
    private var mPhotoSelectedUri: Uri? = null

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnPost.setOnClickListener { postSnapShot() }
        mBinding.btnSelect.setOnClickListener { openGalley() }
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGalley() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    private fun postSnapShot() {
        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val storageReference = mStorageReference.child(PATH_SNAPSHOT).child("my_photo")
        if (mPhotoSelectedUri != null){
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.VISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Photo successfully upload",
                        Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapShot(key, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(mBinding.root, "Photo error upload",
                        Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapShot(key: String, url: String, title: String){
        val snapshot = Snapshot(title = title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RC_GALLERY){
                mPhotoSelectedUri = data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }
}