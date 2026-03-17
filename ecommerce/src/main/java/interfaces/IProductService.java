package interfaces;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IProductService {
    ProductResponse createProduct(ProductRequest request, MultipartFile image);
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image);
    void deleteProduct(Long id);
}