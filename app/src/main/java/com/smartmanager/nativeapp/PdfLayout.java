package com.smartmanager.nativeapp;
/** Deterministic pagination shared by PDF and preview tests. */
public final class PdfLayout { private PdfLayout(){} public static int pages(int cards,int perPage){if(cards<0||perPage<1)throw new IllegalArgumentException();return (cards+perPage-1)/perPage;} public static int firstIndex(int page,int perPage){if(page<0||perPage<1)throw new IllegalArgumentException();return page*perPage;} }
