#!/usr/bin/perl

$sentenceString = "";
foreach $arg (@ARGV) {
	$sentenceString .= "$arg ";
}

$sentenceString = "$sentenceString\.";

print `echo \"$sentenceString\" > sentence.txt`;
exec("./run.sh");
