package com.atguigu.crud.controller;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工增删改查的请求
 */
@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @RequestMapping(value = "/emp/{ids}", method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteEmpById(@PathVariable("ids") String ids){
        //批量删除
        if (ids.contains("-")){
            List<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            for (String s: str_ids
                 ) {
                del_ids.add(Integer.parseInt(s));
            }
            employeeService.deleteBatch(del_ids);
        }else {
            Integer id =Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }

    /**
     * 如果直接发送ajax
     * 的PUT请求，封装的数据除了ID全是null
     * 其实是请求体的数据没有被封装进Employee对象中
     * 究其原因，
     * tomcat会将请求体的数据封装成一个map，通过getParameter取值
     * 但是SpringMVC封装POJO对象的时候，会把每个属性的值调用上述方法拿到，
     * Tomcat识别到你的请求是PUT请求，会不选择封装请求体的数据，所以是null，POST才会封装，SpringMVC的过滤器可解决此问题
     * HttpPutFormContentFilter,这个过滤器会重写getParameter方法，多了个备选的SpringMVC定义的map，可以从中获取请求体中的数据
     * 员工更新
     * @param employee
     * @return
     */
    @RequestMapping(value = "/emp/{empId}", method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee){
        //System.out.println("将要更新的员工数据"+employee);
        employeeService.updateEmp(employee);
        return Msg.success();
    }
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){
        Employee employee = employeeService.getEmp(id);
        return Msg.success().add("emp",employee);
    }

    /**
     * 检查用户名是否可用
     * @param empName
     * @return
     */
    @RequestMapping("/checkuser")
    @ResponseBody
    public Msg checkUser(@RequestParam("empName") String empName){
//        System.out.println("接收到校验了");
        String regx = "(^[a-zA-Z0-9_-]{2,16}$)|(^[\u2E80-\u9FFF]{2,5})";
        if(!empName.matches(regx)){
            return Msg.failed().add("va_msg", "用户名必须是2-16位数字和字母的组合或者2-5位中文");
        }
        boolean b= employeeService.checkUser(empName);
        if (b){
            return Msg.success();
        }else {
            return Msg.failed().add("va_msg", "用户名不可用");
        }
    }
    /**
     * 员工保存
     * 支持JSR303校验，导入H...Validator
     * @return
     * @ResponseBody 返回json数据
     * @Valid 代表封装的数据要进行JSR303的校验,result封装校验结果
     */
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
//        System.out.println("接收到请求了");
        if(result.hasErrors()){
            //校验失败，应该返回失败，在模态框中显示校验失败的错误信息
            Map<String, Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError fieldError : errors) {
                System.out.println("错误的字段名："+fieldError.getField());
                System.out.println("错误信息："+fieldError.getDefaultMessage());
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return Msg.failed().add("errorFields", map);
        }else {
            employeeService.saveEmp(employee);
            return Msg.success();
        }

    }

    /**
     * 需要导入jackson
     * @param pn
     * @return
     */
    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn){
        PageHelper.startPage(pn,5);

//        紧跟着分页查询
        List<Employee> emps = employeeService.getAll();
//        使用pageInfo包装查询后的结果,只需要将PageInfo交给页面即可
//        这里面封装了详细的分页信息，包括有我们查询出来的数据,第二个参数为想显示的页数
        PageInfo page = new PageInfo(emps,5);
        return Msg.success().add("pageInfo",page);
    }
    /**
     * 查询员工数据。分页查询
     * 引入PageHelper分页插件
     * 在查询之前，只需要调用startPage
     * model:存放要交给页面的数据,放在请求域中
     * @return
     */
//    @RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn", defaultValue = "1") Integer pn, Model model){
        PageHelper.startPage(pn,5);

//        紧跟着分页查询
        List<Employee> emps = employeeService.getAll();
//        使用pageInfo包装查询后的结果,只需要将PageInfo交给页面即可
//        这里面封装了详细的分页信息，包括有我们查询出来的数据,第二个参数为想显示的页数
        PageInfo page = new PageInfo(emps,5);

        model.addAttribute("pageInfo",page);
        return "list";
    }
}
