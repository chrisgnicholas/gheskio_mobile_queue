#!/bin/bash

export LD_LIBRARY_PATH=/usr/local/lib
export PATH=/usr/local/bin:${PATH}
export TEMP_IMAGE_PATH=/usr/local/apache2/htdocs/tmp_images
export TEMP_IMAGE_WEB_PATH=/tmp_images

begin_val=
page_val=

# ridiculously crude/rude/fragile html GET parsing 
# of a query string that looks like: begin=xx&pages=yy

params=`echo $QUERY_STRING | sed 's/&/ /g' `
for i in $params
do
#	echo $i
	j=`echo $i | sed 's/begin_val=//g'`
	k=`echo $i | sed 's/page_val=//g'`
	l=`expr match $j "page_val="`

	if [ $l -eq 0 ] 
	then
		begin_val=$j
	else
		page_val=$k
	fi
done

# ridiculously crude way to generate an html page

echo "Content-type: text/html; charset=iso-8859-1"
echo
echo "<html>"
echo "<head> <title>GHESKIO QRcode generation page</title> </head>"
echo "<body>"
echo "<p>"
echo number of pages = $page_val with 9 labels per page
echo "<br>"
echo beginning value = $begin_val
echo "<br>"
# suppose we put 9 per page...
end_val=`expr 9 \* ${page_val} + ${begin_val}`
fake_end_val=`expr 9 \* ${page_val} + ${begin_val} - 1`
echo end_val = $fake_end_val
echo "<br>"
num_codes=`expr 9 \* ${page_val}`
echo num_codes = $num_codes
echo "<br>"
echo "generating labels........"
echo
echo "<br>"


if [ $num_codes -lt 0 ]
then
	echo sorry...end_val must exceed begin_val
	exit -1
fi

if [ $num_codes -gt 300 ]
then
	echo sorry...will only generate in batches less than 300 at a time
	exit -1
fi

current_count=$begin_val
num_codes_sofar=1

final_composite_cmd="convert "

current_batch=""

while [ $current_count -le $end_val ]
do
	remainder=`expr $num_codes_sofar % 9`
#	echo qrencode -s 10 -o /tmp/newfoo${current_count}.png 000000${current_count}_
	qrencode -s 10 -o /tmp/newfoo${current_count}.png 000000${current_count}_
	convert /tmp/newfoo${current_count}.png -gravity North -background White -splice 0x18 -pointsize 30 -annotate +0+10 000000${current_count}_ /tmp/newfoo${current_count}_anno.png
	current_batch="${current_batch} /tmp/newfoo${current_count}_anno.png"
	if [ $remainder -eq 0 ] 
	then
		dividend=`expr $num_codes_sofar / 6`
#		echo montage -geometry 256x256+2+2 ${current_batch} /tmp/montage${dividend}.png
		montage -geometry 256x256+2+2 ${current_batch} /tmp/montage${dividend}.png
		final_composite_cmd="$final_composite_cmd /tmp/montage${dividend}.png"
#		echo rm /tmp/newfoo*.png
		rm /tmp/newfoo*.png
		current_batch=""
	fi

	current_count=`expr $current_count + 1`
	num_codes_sofar=`expr $num_codes_sofar + 1`
done
rm /tmp/newfoo*.png

# echo "************************"
# echo $final_composite_cmd /tmp/composite.pdf
composite_PDF_path=`mktemp -u --tmpdir=${TEMP_IMAGE_PATH}`.pdf
$final_composite_cmd ${composite_PDF_path}

rm /tmp/montage*.png

web_path="/tmp_images/"`basename  ${composite_PDF_path}`

echo "<br>"
echo "your labels are <A href=\"${web_path}\">here</A>"

# echo ${composite_PDF_path}

echo "</body></html>"

