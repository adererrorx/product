package com.kh.product.web;

import com.kh.product.dao.Product;
import com.kh.product.svc.ProductSVC;
import com.kh.product.web.form.DetailForm;
import com.kh.product.web.form.SaveForm;
import com.kh.product.web.form.UpdateForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor  // final멤버 필드를 매개값으로하는 생성자를 자동 생성
public class ProductController {

  private final ProductSVC productSVC;

//  public ProductController(ProductSVC productSVC) {
//    this.productSVC = productSVC;
//  }

  //등록양식
  @GetMapping("/add")
  public String saveForm(Model model){
    SaveForm saveForm = new SaveForm();
    model.addAttribute("saveForm",saveForm);
    return "product/saveForm";
  }

  //등록처리
  @PostMapping("/add")
  public String save(
//      @Param("pname") String pname,
//      @Param("quantity") Long quantity,
//      @Param("price") Long price
//    @ModelAttribute : 1. 요청데이터를 자바객체로 바인딩, 2. Model객체에 추가
      @Valid @ModelAttribute SaveForm saveForm,
      BindingResult bindingResult,  //검증 결과를 담는 객체
      RedirectAttributes redirectAttributes
  ){
//    log.info("pname={}, quantity={}, price={}",pname,quantity,price);
    log.info("saveForm={}",saveForm);

    //데이터 검증
    //어노테이션 기반 검증
    if(bindingResult.hasErrors()){
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    // 필드오류
    if(saveForm.getQuantity() == 100){
      bindingResult.rejectValue("quantity","product");
    }

    // 글로벌오류
    // 총액(상품수량*단가) 1억 초과금지
    if(saveForm.getQuantity() * saveForm.getPrice() > 10_000_000L){
      bindingResult.reject("totalprice",new String[]{"1"},"");
    }

    if(saveForm.getQuantity() * saveForm.getPrice() < 1_000L){
      bindingResult.reject("totalprice2",new String[]{"1"}, "");
    }

    if(saveForm.getQuantity() > 1 && saveForm.getQuantity() <10){
      bindingResult.reject("quantity",new String[]{"1","2"},"");
    }

    if(bindingResult.hasErrors()){
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    //등록
    Product product = new Product();
    product.setPname(saveForm.getPname());
    product.setQuantity(saveForm.getQuantity());
    product.setPrice(saveForm.getPrice());

    Long savedPid = productSVC.save(product);
    redirectAttributes.addAttribute("id",savedPid);
    return "redirect:/products/{id}/detail";
  }

  //조회
  @GetMapping("/{id}/detail")
  public String findById(
      @PathVariable("id") Long id,
      Model model
  ){
    Optional<Product> findedProduct = productSVC.findById(id);
    Product product = findedProduct.orElseThrow();

    DetailForm detailForm = new DetailForm();
    detailForm.setPid(product.getPid());
    detailForm.setPname(product.getPname());
    detailForm.setQuantity(product.getQuantity());
    detailForm.setPrice(product.getPrice());

    model.addAttribute("detailForm",detailForm);
    return "product/detailForm";
  }

  //수정양식
  @GetMapping("/{id}/edit")
  public String updateForm(
      @PathVariable("id") Long id,
      Model model
  ){
    Optional<Product> findedProduct = productSVC.findById(id);
    Product product = findedProduct.orElseThrow();

    UpdateForm updateForm = new UpdateForm();
    updateForm.setPid(product.getPid());
    updateForm.setPname(product.getPname());
    updateForm.setQuantity(product.getQuantity());
    updateForm.setPrice(product.getPrice());

    model.addAttribute("updateForm",updateForm);
    return "product/updateForm";
  }

  //수정
  @PostMapping("/{id}/edit")
  public String update(
      @PathVariable("id") Long pid,
      @Valid @ModelAttribute UpdateForm updateForm,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ){
    //데이터 검증
    if(bindingResult.hasErrors()){
      log.info("bindingResult={}",bindingResult);
      return "product/updateForm";
    }

    //정상 처리
    Product product = new Product();
    product.setPid(pid);
    product.setPname(updateForm.getPname());
    product.setQuantity(updateForm.getQuantity());
    product.setPrice(updateForm.getPrice());

    productSVC.update(pid, product);

    redirectAttributes.addAttribute("id",pid);
    return "redirect:/products/{id}/detail";
  }

  //삭제
  @GetMapping("/{id}/del")
  public String deleteById(@PathVariable("id") Long pid){

    productSVC.delete(pid);

    return "redirect:/products";
  }

  //목록
  @GetMapping
  public String findAll(Model model){

    List<Product> products = productSVC.findAll();
    model.addAttribute("products",products);

    return "product/all";
  }

}