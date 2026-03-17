using DrawMap.WebAPI.DTO;
using DrawMap.WebAPI.Services;
using Microsoft.AspNetCore.Mvc;

namespace DrawMap.WebAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class HeatMapController : ControllerBase
{
    private readonly IHeatMapService _heatMapService;

    public HeatMapController(IHeatMapService heatMapService)
    {
        _heatMapService = heatMapService;
    }

    [HttpGet]
    public async Task<ActionResult<List<HeatMap>>> GetHeatMap(CancellationToken cancellationToken)
    {
        return await _heatMapService.GetHeatMap(cancellationToken);
    }
}