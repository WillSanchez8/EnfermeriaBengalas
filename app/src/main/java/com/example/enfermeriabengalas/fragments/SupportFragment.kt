package com.example.enfermeriabengalas.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.enfermeriabengalas.R
import com.example.enfermeriabengalas.databinding.FragmentSupportBinding

class SupportFragment : Fragment() {

    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSupportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
    }

    private fun setButtonClickListener(button: ImageButton, url: String) {
        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun registerEvents() {
        setButtonClickListener(binding.btnIcons8, "https://icons8.com/icons/")
        setButtonClickListener(binding.btnPrepa, "https://www.facebook.com/profile.php?id=100064645933379")
        setButtonClickListener(binding.btnFace1, "https://www.facebook.com/profile.php?id=100004820164895")
        setButtonClickListener(binding.btnWhats1, "https://api.whatsapp.com/send?phone=525514185533")
        setButtonClickListener(binding.btnGithub1, "https://github.com/WillSanchez8")
        setButtonClickListener(binding.btnFace2, "https://www.facebook.com/anel.ariana.1")
        setButtonClickListener(binding.btnWhats2, "https://api.whatsapp.com/send?phone=525581769032")
        setButtonClickListener(binding.btnGithub2, "https://github.com/AnelAriana")
        setButtonClickListener(binding.btnTescha, "https://tescha.edomex.gob.mx/")
    }
}