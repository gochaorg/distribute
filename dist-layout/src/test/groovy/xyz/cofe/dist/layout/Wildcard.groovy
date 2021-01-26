package xyz.cofe.dist.layout

import groovy.transform.CompileStatic

import java.util.regex.Pattern

class Wildcard {
    @CompileStatic
    static Pattern wildcard( String ptrn,boolean ignoreCase=true ){
        if( ptrn==null )throw new IllegalArgumentException( "ptrn==null" );

        List<String> rx = []
        int state = 0
        StringBuilder buff = new StringBuilder()

        def build = {
            if( buff.length()<1 )return;

            switch (state){
                case 0:
                    rx.add( Pattern.quote(buff.toString()) )
                    break
                case 1:
                    if( buff.toString()=='*' ){
                        rx.add('.*')
                    }
                    break
            }

            buff.setLength(0)
        }

        for( int ci=0; ci<ptrn.length(); ci++ ){
            char c0 = ptrn.charAt(ci)
            switch (state){
                case 0:
                    switch (c0){
                        case '*':
                            build()
                            state = 1;
                            buff.append(c0)
                            break
                        case '\\':
                            state = 10
                            break
                        default:
                            buff.append(c0)
                            break
                    }
                    break
                case 1:
                    build()
                    state = 0
                    buff.append(c0)
                    break
                case 10:
                    buff.append(c0)
                    state = 0
                    break
            }
        }
        build()

        if( ignoreCase ){
            rx.add(0,'(?is)')
        }

        Pattern.compile( rx.join('') )
    }
}
