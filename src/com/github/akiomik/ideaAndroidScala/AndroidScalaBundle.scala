package com.github.akiomik.ideaAndroidScala

import java.lang.ref.{Reference, SoftReference}
import java.util.ResourceBundle

import com.intellij.CommonBundle

/**
 * Created by akiomi on 15/02/24.
 */

object AndroidScalaBundle {
  def apply(key: String, params: AnyRef*) = CommonBundle.message(get(), key, params: _*)

  private var ourBundle: Reference[ResourceBundle] = null
  private val BUNDLE = "com.github.akiomik.ideaAndroidScala.AndroidScalaBundle"

  private def get(): ResourceBundle = {
    var bundle: ResourceBundle = null
    if (ourBundle != null) bundle = ourBundle.get
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE)
      ourBundle = new SoftReference[ResourceBundle](bundle)
    }
    bundle
  }
}
