package interview.guide;

import java.util.ArrayList;
import java.util.List;


public class test {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        List<Integer> list2=List.of(0,3,9,10);
        List<Integer> ans=merge(list,list2);
        System.out.println(ans);
    }

    static  public List<Integer> merge(List<Integer> list,List<Integer> list2){{
        List<Integer> ans=new ArrayList<>();
        int i=0,j=0;
        int n=list.size(),m=list2.size();;
        while(i<n&&j<m){
            if(list.get(i)<list2.get(j)){
                ans.add(list.get(i));
                i++;
            }else{
                ans.add(list2.get(j));
                j++;
            }
        }
        if(i<n){
            ans.addAll(list.subList(i,n));
        }
        if(j<m){
            ans.addAll(list2.subList(j,m));
        }
        return ans;


    }}
}
