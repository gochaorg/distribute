package xyz.cofe.dist.layout

import java.nio.file.Path
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class ShellScript {
    Path shellScript
    String mainClass
    Path jars
    abstract String generate()

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern('yyyy-MMM-dd HH:mm:ss', Locale.ENGLISH)
    public String format(Date date){
        if( date==null )throw new IllegalArgumentException( "date==null" );
        dateTimeFormatter.format(date.toInstant().atZone(ZoneId.systemDefault()))
    }
}
