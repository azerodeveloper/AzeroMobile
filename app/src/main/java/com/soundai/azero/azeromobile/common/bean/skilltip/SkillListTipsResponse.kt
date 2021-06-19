package com.soundai.azero.azeromobile.common.bean.skilltip

data class Item(val title:String,val tips:List<String>)
data class SkillListTipsResponse(val type:String,val items: List<Item>)