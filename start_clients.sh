DIR="$( cd "$( ./CS455/HW2/Test3 "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/CS455HW2.jar"
MACHINE_LIST="$DIR/conf/machine_list"
SCRIPT="java -cp $JAR_PATH cs455.scaling.client.Client lima 55555 4"
COMMAND='gnome-terminal --geometry=200x40'

for machine in`cat $MACHINE_LIST`
do
   OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
   COMMAND+=" $OPTION"
done
for machine in`cat $MACHINE_LIST`
do
   OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
   COMMAND+=" $OPTION"
done
for machine in`cat $MACHINE_LIST`
do
   OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
   COMMAND+=" $OPTION"
done
for machine in`cat $MACHINE_LIST`
do
   OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
   COMMAND+=" $OPTION"
done
eval $COMMAND &