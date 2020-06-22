/*
 * Created by Murillo Comino on 18/06/20 20:56
 * Github: github.com/onimur
 * StackOverFlow: pt.stackoverflow.com/users/128573
 * Email: murillo_comino@hotmail.com
 *
 *  Copyright (c) 2020.
 *  Last modified 18/06/20 20:44
 */

package br.com.comino.examplecoroutinestask

import android.content.Intent
import br.com.comino.choicekotlinjava.BaseChoiceActivity
import br.com.comino.choicekotlinjava.Choice

class ChoiceActivity : BaseChoiceActivity() {

    override val choices: List<Choice>
        get() = listOf(
            Choice(
                "My Question",
                "My question from Kotlin Discussion and StackOverFlow",
                Intent(this, MyQuestionActivity::class.java)
            ),
            Choice(
                "Nickallendev's reply",
                "Nickallendev's reply from Kotlin Discussion",
                Intent(this, NickallendevReplyActivity::class.java)
            ),
            Choice(
                "Circusmagnus's reply",
                "Circusmagnus's reply from StackOverFlow",
                Intent(this, CircusmagnusReplyActivity::class.java)
            ),
            Choice(
                "Omid Faraji's reply",
                "Omid Faraji's reply from StackOverFlow",
                Intent(this, OmidFarajiReplyActivity::class.java)
            )
        )
}