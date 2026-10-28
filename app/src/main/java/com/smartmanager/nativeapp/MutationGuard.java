package com.smartmanager.nativeapp;
import java.util.*;
/** Validates a resource snapshot before a write; it does not issue network commands. */
public final class MutationGuard { private MutationGuard(){} public static Map<String,String> requireId(List<Map<String,String>> rows,String id)throws java.io.IOException{if(id==null||id.trim().isEmpty())throw new java.io.IOException("معرف المورد مطلوب");for(Map<String,String> row:rows)if(id.equals(row.get(".id")))return row;throw new java.io.IOException("المورد غير موجود أو تغير");} }
